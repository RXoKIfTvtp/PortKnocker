package knocker.lib.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Provides a simpler interface for RSA encryption
**/
public class RSA {
	
	/**
	 * The cipher mode to be used by {@link RSA#encrypt(byte[], Key) encrypt()} and {@link RSA#decrypt(byte[], Key) decrypt()}
	**/
	// RSA/ECB/OAEPWithSHA-1AndMGF1Padding can't be used for signing
	private final static String CIPHER_MODE = "RSA/ECB/PKCS1Padding";
	
	/**
	 * Determins if an integer is a power of two
	 * @param x A positive integer
	 * @return true if {@code x} is positive and a power of two
	 */
	private static boolean isPowerOfTwo(int x) {
		return ((x > 0) && (x & (x - 1)) == 0);
	}
	
	/**
	 * Generates a Keypair with the specified amount of bits.
	 * If bits is not a power of two or is less than 1024, a 2048 bit key is generated
	 * @param bits The desired amount of bits
	 * @return A {@link java.security.KeyPair KeyPair} to use for RSA encryption and decryption
	 * @throws NoSuchAlgorithmException
	**/
	public final static KeyPair gen(int bits) throws NoSuchAlgorithmException {
		if (bits < 1024 || !isPowerOfTwo(bits)) {
			bits = 2048;
		}
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(bits);
		return keyGen.generateKeyPair();
	}
	
	/**
	 * Loads an X.509 encoded key from the given bytes
	 * @param keyBytes The bytes of the key
	 * @return A {@link java.security.PrivateKey PrivateKey} for RSA encryption
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	**/
	public static PrivateKey privateKeyFrom(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(ks);
	}
	
	/**
	 * Loads an X.509 encoded key from the given bytes
	 * @param keyBytes The bytes of the key
	 * @return A {@link java.security.PublicKey PublicKey} for RSA encryption
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	**/
	public static PublicKey publicKeyFrom(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(ks);
	}
	
	/**
	 * RSA encrypts the given bytes with the given key
	 * @param bytes The bytes to encrypt
	 * @param key The key to use for encryption
	 * @return The encrypted bytes
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] encrypt(byte[] bytes, Key key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(bytes);
	}
	
	/**
	 * RSA decrypts the given bytes with the given key
	 * @param bytes The bytes to decrypt
	 * @param key The key to use for decryption
	 * @return The decrypted bytes
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] decrypt(byte[] bytes, Key key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(bytes);
	}
}
