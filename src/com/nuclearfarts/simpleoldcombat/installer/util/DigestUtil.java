package com.nuclearfarts.simpleoldcombat.installer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String sha1(File file) throws IOException {
		MessageDigest hasher = null;
		try {
			hasher = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try (InputStream fis = new FileInputStream(file)) {
			int n = 0;
			byte[] buffer = new byte[8192];
			while (n != -1) {
				n = fis.read(buffer);
				if (n > 0) {
					hasher.update(buffer, 0, n);
				}
			}
		}
		return bytesToHex(hasher.digest());
	}

	/**
	 * Thanks for removing DataTypeConverter from the standard JDK with NO
	 * ALTERNATIVE BUT TO WRITE YOUR OWN, Oracle. People TOTALLY wanted to lose a
	 * basic feature like converting a byte array to a hex string.
	 * 
	 * And they wonder why everyone hates them.
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
