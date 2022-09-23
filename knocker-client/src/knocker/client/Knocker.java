package knocker.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import knocker.lib.io.Base64;
import knocker.lib.security.AES;
import knocker.lib.security.RSA;

public class Knocker {
	
	/**
	 * Stores the options for the knock<br>
	 * {@link Options#rhost rhost}<br>
	 * {@link Options#rport rport}<br>
	 * {@link Options#lhost lhost}<br>
	 * {@link Options#lport lport}<br>
	 * {@link Options#key key}<br>
	 * {@link Options#execute execute}<br>
	 * <br>
	 * {@link Options#rhost rhost} and {@link Options#rport rport} must always be set otherwise the host to knock at is unknown.<br>
	**/
	public static class Options {
		/**
		 * Stores the host name or IP address of the remote machine where the knock should be sent.
		 * {@link lport lport} must also be set.
		**/
		public String rhost = null;
		
		/**
		 * Stores the port of the remote machine where the knock should be sent.
		 * {@link rhost rhost} must also be set.
		**/
		public int rport = -1;
		
		/**
		 * Stores the host name or IP address of the machine where the {@link rhost rhost} should connect back to if the knock is accepted.
		 * If {@link lhost lhost} is not set, {@link rhost rhost} will attempt to reply to the IP address where the knock was received from.
		**/
		public String lhost = null;
		
		/**
		 * Stores the port of the machine where the {@link rhost rhost} should connect back to if the knock is accepted.
		**/
		public int lport = -1;
		
		/**
		 * Stores the private key with which the knock will be signed.
		**/
		public PrivateKey key = null;
		
		/**
		 * Stores a command that is executed using {@code java.lang.ProcessBuilder(command).start();} on
		 * the {@link rhost rhost}, if the knock is accepted.
		 * If {@link lport lport} is set then the command will not
		 * be executed. The output of command is not returned by {@link rhost rhost}
		 * @see {@link java.lang.ProcessBuilder(String)}
		 */
		public String execute = null;
	}
	
	private final static char FALSE = '@'; // 0b01000000 = 0x40 = '@'
	private final static char TRUE = 'A'; // 0b01000001 = 0x41 = 'A'
	
	private final static char OP_REVERSE = 'B'; // 0b01000010 = 0x42 = 'B'
	private final static char OP_EXECUTE = 'D'; // 0b01000100 = 0x44 = 'D'
	
	/**
	 * Sends a knock to a remote machine with the specified options
	 * @param o The {@link Options Options} to use for the knock
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws SocketException
	 * @throws UnknownHostException
	**/
	public static void send(Options o) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		if (o.rhost == null) {
			throw new IllegalArgumentException("No remote host specified.");
		}
		if (o.rport == -1) {
			throw new IllegalArgumentException("No remote port specified.");
		}
		InetAddress a = InetAddress.getByName(o.rhost);
		
		StringBuilder sb = new StringBuilder();
		StringBuilder tmp = new StringBuilder();
		
		if (o.lport != -1) {
			tmp.append(OP_REVERSE);
			if (o.lhost != null) {
				tmp.append(o.lhost);
				tmp.append(':');
			}
			tmp.append(o.lport);
		} else if (o.execute != null) {
			tmp.append(OP_EXECUTE);
			tmp.append(o.execute);
		} else {
			throw new IllegalArgumentException("No action specified for remote machine.");
		}
		
		if (o.key != null) {
			byte[] key = AES.gen();
			byte[] t;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			
			t = RSA.encrypt(key, o.key);
			dos.writeShort(t.length);
			dos.write(t);
			
			t = AES.encrypt(key, tmp.toString().getBytes("UTF-8"));
			dos.writeShort(t.length);
			dos.write(t);
			
			sb.append(TRUE); // Is this knock encrypted?
			sb.append(new String(Base64.encode(baos.toByteArray()))); // The encrypted knock
		} else {
			sb.append(FALSE); // Is this knock encrypted?
			sb.append(tmp); // The plain-text knock
		}
		
		byte[] b = sb.toString().getBytes();
		DatagramPacket p = new DatagramPacket(b, b.length, a, o.rport);
		DatagramSocket s = new DatagramSocket();
		s.send(p);
		s.close();
	}
}
