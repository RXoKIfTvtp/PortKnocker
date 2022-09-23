package knocker.lib.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides a simpler interface for AES encryption
**/
public class AES {
	
	/**
	 * The cipher mode to be used by {@link AES#encrypt(byte[], byte[]) encrypt()} and {@link AES#decrypt(byte[], byte[]) decrypt()}
	**/
	private final static String CIPHER_MODE = "AES/CBC/PKCS5Padding";
	
	/**
	 * Generates a 128-bit key using {@link java.security.SecureRandom SecureRandom}
	 * @return 16 random bytes
	**/
	public static byte[] gen() {
		SecureRandom sr = new SecureRandom();
		byte[] key = new byte[16]; // 128 bit key
		sr.nextBytes(key);
		return key;
	}
	
	/**
	 * AES encrypts the bytes using the given key
	 * @param key The key to use for encryption. Must be 16 bytes (128 bits).
	 * @param bytes The bytes to encrypt
	 * @return The encrypted bytes
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws IOException 
	**/
	public static byte[] encrypt(byte[] key, byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		if (key == null || key.length != 16) {
			throw new InvalidKeyException("Key size must be 128 bits.");
		}
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = gen();
        IvParameterSpec paramSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
		byte[] en = cipher.doFinal(bytes);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(iv);
		stream.write(en);
		stream.close();
		return stream.toByteArray();
	}
	
	/**
	 * AES decrypts the bytes using the given key
	 * @param key The key to use for decryption. Must be 16 bytes (128 bits).
	 * @param bytes The bytes to decrypt
	 * @return The decrypted bytes
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException 
	**/
	public static byte[] decrypt(byte[] key, byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		if (key == null || key.length != 16) {
			throw new InvalidKeyException("Key size must be 128 bits.");
		}
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		byte[] iv = new byte[16];
		System.arraycopy(bytes, 0, iv, 0, 16);
        IvParameterSpec paramSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
		return cipher.doFinal(bytes, 16, bytes.length - 16);
	}
}
