package knocker.lib.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Used for reading and writing files
**/
public class Loader {
	
	/**
	 * Attempts to get all the bytes in a file and return it as a byte array.
	 * @param file The file to read
	 * @return The all the bytes in a file or null if the file could not be fully read
	**/
	public static byte[] read(File file) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream is = null;
		
		try {
			is = new FileInputStream(file);
			byte[] buf = new byte[4096];
			int read;
			
			while ((read = is.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			
			return baos.toByteArray();
		} catch (IOException e) {
			// Swallow
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
		return null;
	}
	
	/**
	 * Attempts to write all the bytes to the file
	 * @param file The file to write to
	 * @param bytes The bytes to write to the file
	 * @return true if all bytes were written to the file, otherwise false
	**/
	public static boolean write(File file, byte[] bytes) {
		FileOutputStream fos = null;
		boolean ret = false;
		try {
			fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			ret = true;
		} catch (IOException e) {
			// Swallow
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
		return ret;
	}
}
