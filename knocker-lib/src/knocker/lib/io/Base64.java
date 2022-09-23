package knocker.lib.io;

/**
 * A custom Base64 encoder and decoder to provide backward compatibility with Java 6
**/
public class Base64 {

	private Base64() {
		
	}
	
	/**
	 * Base-64 encodes a byte array
	 * @param src The raw bytes to base-64 encode
	 * @return The base-64 encoded bytes
	**/
	public static byte[] encode(byte[] src) {
		if (src == null) {
			return null;
		}
		byte data[] = new byte[src.length + 2];
		System.arraycopy(src, 0, data, 0, src.length);
		byte dst[] = new byte[(data.length / 3) * 4];
		// 3-byte to 4-byte conversion
		for (int sidx = 0, didx = 0; sidx < src.length; sidx += 3, didx += 4) {
			dst[didx] = (byte) ((data[sidx] >>> 2) & 077);
			dst[didx + 1] = (byte) ((data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077);
			dst[didx + 2] = (byte) ((data[sidx + 2] >>> 6) & 003 | (data[sidx + 1] << 2) & 077);
			dst[didx + 3] = (byte) (data[sidx + 2] & 077);
		}
		// 0-63 to printable ascii conversion
		for (int idx = 0; idx < dst.length; idx++) {
			if(dst[idx] < 26)
				dst[idx] = (byte)(dst[idx] + 'A');
			else if(dst[idx] < 52)
				dst[idx] = (byte)(dst[idx] + 'a' - 26);
			else if(dst[idx] < 62)
				dst[idx] = (byte)(dst[idx] + '0' - 52);
			else if(dst[idx] < 63)
				dst[idx] = (byte)'+';
			else
				dst[idx] = (byte)'/';
		}
		// add padding
		for (int idx = dst.length - 1; idx > (src.length * 4) / 3; idx--) {
			dst[idx] = (byte)'=';
		}
		return dst;
	}
	
	/**
	 * Base-64 decodes a byte array
	 * @param src The base-64 encoded bytes
	 * @return The raw bytes
	**/
	public static byte[] decode(byte[] src) {
		int tail = src.length;
		while (src[tail-1] == '=') {
			tail--;
		}
		byte dst[] = new byte[tail - src.length / 4];
		// ascii printable to 0-63 conversion
		for (int idx = 0; idx < src.length; idx++) {
			if (src[idx] == '=')
				src[idx] = 0;
			else if (src[idx] == '/')
				src[idx] = 63;
			else if (src[idx] == '+')
				src[idx] = 62;
			else if (src[idx] >= '0' && src[idx] <= '9')
				src[idx] = (byte)(src[idx] - ('0' - 52));
			else if (src[idx] >= 'a' && src[idx] <= 'z')
				src[idx] = (byte)(src[idx] - ('a' - 26));
			else if (src[idx] >= 'A' && src[idx] <= 'Z')
				src[idx] = (byte)(src[idx] - 'A');
		}
		
		// 4-byte to 3-byte conversion
		int sidx, didx;
		for (sidx = 0, didx = 0; didx < dst.length - 2; sidx += 4, didx += 3) {
			dst[didx] = (byte) (((src[sidx] << 2) & 255) | ((src[sidx + 1] >>> 4) & 3));
			dst[didx + 1] = (byte) (((src[sidx + 1] << 4) & 255) | ((src[sidx + 2] >>> 2) & 017));
			dst[didx + 2] = (byte) (((src[sidx + 2] << 6) & 255) | (src[sidx + 3] & 077));
		}
		if (didx < dst.length) {
			dst[didx] = (byte) (((src[sidx] << 2) & 255) | ((src[sidx + 1] >>> 4) & 3));
		}
		if (++didx < dst.length) {
			dst[didx] = (byte) (((src[sidx + 1] << 4) & 255) | ((src[sidx + 2] >>> 2) & 017));
		}
		return dst;
	}
	
	/**
	 * See {@link Base64#encode(byte[]) encode(byte[])}
	 * @see encode(byte[])
	**/
	public static byte[] encode(String s) {
		if (s == null) {
			return null;
		}
		try {
			return encode(s.getBytes("UTF-8"));
		} catch (Exception e) {
			return encode(s.getBytes());
		}
	}
	
	/**
	 * See {@link Base64#decode(byte[]) decode(byte[])}
	 * @see decode(byte[])
	**/
	public static byte[] decode(String s) {
		if (s == null) {
			return null;
		}
		return decode(s.getBytes());
	}
}