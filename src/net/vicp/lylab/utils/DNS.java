package net.vicp.lylab.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import net.vicp.lylab.core.CloneableBaseObject;

public class DNS extends CloneableBaseObject {
	protected static Map<String, String> DNSCache;
	
	public static void main(String[] args) throws UnknownHostException {
		System.out.println(InetAddress.getByName("www.baidu.com"));
		System.out.println(InetAddress.getByName("www.baidu.com").getHostAddress());
		System.out.println(domainNameService("http://www.baidu.com/fdsaewq"));
		System.out.println(isIP(domainNameService("http://www.ebaolife.com/fdsaewq")));
		System.out.println(isIP("192.168.0.12201212"));
		System.out.println(isIP("222.16.228.2ba"));
	}
	
	public static String domainNameService(String domain) throws UnknownHostException {
		domain = domain.replaceAll("^[a-zA-Z]*://", "").replaceAll("/[\\S]*", "");
		String ip = InetAddress.getByName(domain).getHostAddress();
		return ip;
	}

	public static boolean isIP(String ipAddr) {
		return ipAddr.matches(
				"((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
	}
	
}
