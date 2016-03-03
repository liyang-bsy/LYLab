package net.vicp.lylab.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import net.vicp.lylab.core.CloneableBaseObject;

public abstract class DNS extends CloneableBaseObject {
	protected static Map<String, String> DNSCache;

	public final static String domainNameService(String domain) throws UnknownHostException {
		domain = domain.replaceAll("^[a-zA-Z]*://", "").replaceAll("/[\\S]*", "");
		return InetAddress.getByName(domain).getHostAddress();
	}

	public final static boolean isIP(String ipAddr) {
		return ipAddr.matches(
				"((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
	}

}
