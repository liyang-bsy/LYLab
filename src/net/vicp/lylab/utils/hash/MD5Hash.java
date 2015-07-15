package net.vicp.lylab.utils.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.DoHash;

public final class MD5Hash extends NonCloneableBaseObject implements DoHash {

	private MessageDigest md5;
	public MD5Hash() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) { }
	}

	@Override
	public int hash(String key) {
		synchronized (lock) {
			md5.reset();
			md5.update(key.getBytes());
			byte[] bytes = md5.digest();
			
			int hash = 0;
			for(byte b:bytes)
				hash += (int)b;
			return hash;
		}
	}

}
