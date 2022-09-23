package knocker.server;

import java.net.SocketException;

public class Main {
	
	public static void main(String[] args) {
		/**
		 * a default port to bind to
		**/
		int port = 5555;
		
		/**
		 * See Server.encryptionRequired
		**/
		boolean encryptionRequired = true;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-u") || args[i].equalsIgnoreCase("-unsecure")) {
				encryptionRequired = false;
			} else if (args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-key") || args[i].equalsIgnoreCase("-keys")) {
				String path = args[++i];
				if (!Decryptor.init(path)) {
					System.out.println("Failed to load any keys from \"" + path + "\"");
					return;
				}
			} else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-port")) {
				try {
					port = Integer.parseInt(args[++i]);
				} catch (Exception e) {
					System.out.println("Invalid port \"" + args[i] + "\" specified.");
					return;
				}
			} else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help")) {
				System.out.println("[-u|-unsecure]      Disables security and authentication.");
				System.out.println("[-k|-key|-keys]     Specifies a key file or folder for auth.");
				System.out.println("[-p|-port]          Specifies the port to listen for knocks on.");
			}
		}
		
		if (encryptionRequired && Decryptor.keyCount() == 0) {
			System.err.println("Server is set to secure but no authorized keys have been found!");
			System.err.println("No knocks will be accepted without at least one authorized key.");
			return;
		}
		
		try {
			Server.init(port, encryptionRequired);
		} catch (SocketException e) {
			System.out.println("Failed to bind to port " + port);
		}
	}
}
