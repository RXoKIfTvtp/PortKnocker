package knocker.client;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import knocker.client.Knocker.Options;
import knocker.lib.io.Loader;
import knocker.lib.security.RSA;

public class Main {
	
	public static void main(String[] args) throws IOException {
		Options o = new Options();
		
		// A few default options for key generation
		boolean gen = false;
		int bits = 2048;
		String output = null;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-rh") || args[i].equalsIgnoreCase("-rhost")) {
				o.rhost = args[++i];
			} else if (args[i].equalsIgnoreCase("-rp") || args[i].equalsIgnoreCase("-rport")) {
				try {
					o.rport = Integer.parseInt(args[++i]);
				} catch (Exception e) {
					System.out.println("Invalid remote port \"" + args[i] + "\" specified.");
					return;
				}
			} else if (args[i].equalsIgnoreCase("-lh") || args[i].equalsIgnoreCase("-lhost")) {
				o.lhost = args[++i];
			} else if (args[i].equalsIgnoreCase("-lp") || args[i].equalsIgnoreCase("-lport")) {
				try {
					o.lport = Integer.parseInt(args[++i]);
				} catch (Exception e) {
					System.out.println("Invalid local port \"" + args[i] + "\" specified.");
					return;
				}
			} else if (args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-key")) {
				String file = args[++i];
				try {
					o.key = RSA.privateKeyFrom(Loader.read(new File(file)));
				} catch (NoSuchAlgorithmException e) {
					System.err.println("This Java Runtime Environment doesn't support RSA encrption.");
					return;
				} catch (InvalidKeySpecException e) {
					System.err.println("\'" + file + "\" is not a valid RSA private key.");
					return;
				}
			} else if (args[i].equalsIgnoreCase("-e") || args[i].equalsIgnoreCase("-execute")) {
				o.execute = args[++i];
			} else if (args[i].equalsIgnoreCase("-g") || args[i].equalsIgnoreCase("-gen") || args[i].equalsIgnoreCase("-generate")) {
				gen = true;
				try {
					bits = Integer.parseInt(args[i + 1]);
					i++;
				} catch (Exception e) {
					// Swallow
					// If this happens the default amount of bits will be used.
				}
			} else if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("-out") || args[i].equalsIgnoreCase("-output")) {
				if (!args[i + 1].startsWith("-")) {
					output = args[++i];
				}
			} else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help")) {
				System.out.println("[-rh|-rhost]        The remote host to send a knock to.");
				System.out.println("[-rp|-rport]        The port on the rhost.");
				System.out.println("[-lh|-lhost]        The host the rhost should connect back to.");
				System.out.println("[-lp|-lport]        The port on the lhost.");
				System.out.println("[-k|-key]           The key to use for the knock.");
				System.out.println("[-e|-execute]       The command to execute on the rhost.");
				System.out.println("[-g|-gen|-generate] The amount of bits to use for the generate RSA keys.");
				System.out.println("[-o|-out|-output]   The output directory for the generate RSA keys.");
				return;
			} else {
				System.out.println("Unknown argument: " + args[i]);
				return;
			}
		}
		
		if (gen) {
			try {
				boolean result = Generator.create(bits, output);
				if (result) {
					System.out.println("Successfully generated keys.");
				} else {
					System.err.println("Failed to generate keys.");
				}
			} catch (NoSuchAlgorithmException e) {
				System.err.println("Failed to generate keys.");
				System.err.println("This Java Runtime Environment doesn't support RSA encryption.");
			}
		} else {
			try {
				Knocker.send(o);
				System.out.println("Successfully sent knock.");
			} catch (UnknownHostException e) {
				System.err.println("Unknown host: " + o.rhost);
			} catch (SocketException e) {
				System.err.println("Failed to connect to host: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				System.err.println("Failed to send data to host: " + e.getMessage());
			} catch (Exception e) {
				System.err.println("An unexpected error has occured: " + e.getMessage());
			}
		}
	}
}
