package knocker.client;

import java.io.File;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import knocker.lib.io.Loader;
import knocker.lib.security.RSA;

public class Generator {
	
	/**
	 * Generates an RSA key-pair and saves them to a specified output.
	 * @param bits The bit size of a key
	 * @param output The path to save the keys to
	 * @return true if a key was created and saved otherwise false
	 * @throws NoSuchAlgorithmException
	**/
	public static boolean create(int bits, String output) throws NoSuchAlgorithmException {
		KeyPair kp = RSA.gen(bits);
		boolean ret = false;
		ret |= Loader.write(new File(output, "public-key.pub"), kp.getPublic().getEncoded());
		ret |= Loader.write(new File(output, "private-key.pri"), kp.getPrivate().getEncoded());
		return ret;
	}
}
