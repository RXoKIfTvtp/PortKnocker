package knocker.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is used to pipe the bytes from an InputStream to an
 * OutputStream without buffering or waiting for line terminators.
 * The pipe will persist until either the InputStream or the
 * OutputStream has closed.
**/
public class Pipe extends Thread {
	
	/**
	 * The InputStream to read bytes from 
	**/
	private InputStream input;
	
	/**
	 * The OutputStream to write bytes to from the input
	**/
	private OutputStream output;
	
	/**
	 * Pipes the bytes from the input to the output
	 * @param input The InputStream to read bytes from 
	 * @param output The OutputStream to write bytes to from the input
	 */
	public Pipe(InputStream input, OutputStream output) {
		this.input = input;
		this.output = output;
		this.start();
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int read;
		try {
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
				output.flush();
			}
		} catch (Exception e) {
			// Swallow
		} finally {
			try {
				this.input.close();
			} catch (IOException e) {
				// Swallow
			}
			try {
				this.output.close();
			} catch (IOException e) {
				// Swallow
			}
		}
	}
}
