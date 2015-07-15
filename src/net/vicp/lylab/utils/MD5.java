package net.vicp.lylab.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.vicp.lylab.core.NonCloneableBaseObject;

public final class MD5 extends NonCloneableBaseObject {
	
	private static MessageDigest md5;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) { }
	}

	public static String md5_32(String plainText) {
		StringBuffer buf = new StringBuffer("");
		byte b[];
		synchronized (md5) {
			md5.reset();
			md5.update(plainText.getBytes());
			b = md5.digest();
		}
		int i;
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();
	}
	
	public static String md5_16(String plainText) {
		StringBuffer buf = new StringBuffer("");
		byte b[];
		synchronized (md5) {
			md5.reset();
			md5.update(plainText.getBytes());
			b = md5.digest();
		}
		int i;
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}
		buf.toString().substring(8, 24);
		return buf.toString().substring(8, 24);
	}
}
