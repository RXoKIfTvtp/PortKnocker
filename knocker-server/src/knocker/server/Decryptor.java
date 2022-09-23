package knocker.server;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import knocker.lib.io.Loader;
import knocker.lib.security.RSA;

public class Decryptor {
	
	/**
	 * Stores all authorized keys
	 */
	private static ArrayList<PublicKey> keys = new ArrayList<PublicKey>();
	
	/**
	 * Attempts to load a public key into {@link keys keys}
	 * @param file The file containing a key to try load.
	 * @return true if a key was successfully loaded from the file, otherwise false.
	 */
	private static boolean tryLoad(File file) {
		if (file.getName().toLowerCase().endsWith(".pub")) {
			byte[] bytes = Loader.read(file);
			if (bytes != null) {
				try {
					PublicKey key = RSA.publicKeyFrom(bytes);
					keys.add(key);
					return true;
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					// Should never happen unless JRE doesn't support RSA
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
					// Should never happen unless JRE doesn't support RSA
				}
			} else {
				
			}
		}
		return false;
	}
	
	/**
	 * Loads keys from a specified path which may be a file or folder.
	 * public key files must have a ".pub" extension
	 * @param path A path to a key file or folder of keys
	 * @return true if at least one key was successfully loaded.
	 */
	public static boolean init(File path) {
		ArrayList<File> queue = new ArrayList<File>();
		boolean ret = false;
		queue.add(path);
		while (queue.size() > 0) {
			path = queue.remove(0);
			if (path.exists()) {
				if (path.isFile()) {
					if (tryLoad(path)) {
						ret = true;
					}
				} else if (path.isDirectory()) {
					File[] list = path.listFiles();
					for (int i = 0; i < list.length; i++) {
						queue.add(list[i]);
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * See {@link Decryptor#init(File) Decryptor.init(File)}
	 * @see Decryptor.init(File)
	 */
	public static boolean init(String path) {
		return init(new File(path));
	}
	
	/**
	 * Attemps to decrypt an aes key with any of the authorized {@link Decryptor#keys keys}
	 * @param bytes The RSA-encrypted AES-key bytes.
	 * @return The decrypted aes key or null if the bytes could not be decrypted.
	 */
	public static byte[] tryDecrypt(byte[] bytes) {
		for (int i = 0; i < keys.size(); i++) {
			try {
				byte[] d = RSA.decrypt(bytes, keys.get(i));
				return d;
			} catch (Exception e) {
				// Swallow
			}
		}
		return null;
	}
	
	/**
	 * The amount of currently authorized keys that have been loaded
	 * @return An integer, the amount of keys loaded
	 */
	public static int keyCount() {
		return keys.size();
	}
}
