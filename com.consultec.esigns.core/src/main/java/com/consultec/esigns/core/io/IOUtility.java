package com.consultec.esigns.core.io;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

/**
 * The Class IOUtility.
 *
 * @author hrodriguez
 */
public class IOUtility {

	
	/**
	 * Instantiates a new IO utility.
	 */
	private IOUtility() {}
	
	/**
	 * Write decoded content.
	 *
	 * @param path
	 *            the path
	 * @param encoded
	 *            the encoded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void writeDecodedContent(String path, String encoded) throws IOException {
		byte[] rawDecoded = Base64.getDecoder().decode(encoded);
		FileUtils.writeByteArrayToFile(new File(path), rawDecoded);
	}

	/**
	 * Write encoded content.
	 *
	 * @param path
	 *            the path
	 * @param encoded
	 *            the encoded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void writeContentToFile(String path, String encoded) throws IOException {
		FileUtils.write(new File(path), encoded);
	}

	/**
	 * Write encoded content.
	 *
	 * @param path
	 *            the path
	 * @param rawDecoded
	 *            the raw decoded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void writeEncodedContent(String path, byte[] rawDecoded) throws IOException {
		byte[] encoded = Base64.getEncoder().encode(rawDecoded);
		FileUtils.writeByteArrayToFile(new File(path), encoded);
	}

	/**
	 * Bytes to hex.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the string
	 */
	public static String bytesToHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}
