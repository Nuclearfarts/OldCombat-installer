package com.nuclearfarts.simpleoldcombat.installer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class DigestUtil {

	public static String sha1(File file) throws IOException {
		MessageDigest hasher = null;
		try {
			hasher = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
	    try(InputStream fis = new FileInputStream(file)) {
	    	int n = 0;
	    	byte[] buffer = new byte[8192];
	    	while (n != -1) {
	        	n = fis.read(buffer);
	        	if (n > 0) {
	        		hasher.update(buffer, 0, n);
	        	}
	    	}
	    }
	    return new HexBinaryAdapter().marshal(hasher.digest());
	}

}
