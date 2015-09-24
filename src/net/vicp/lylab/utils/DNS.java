package net.vicp.lylab.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNS {
	public static void main(String[] args) throws UnknownHostException {
		System.out.println(InetAddress.getByName("www.baidu.com"));
		System.out.println(InetAddress.getByName("www.baidu.com").getHostAddress());
		System.out.println(domainNameService("http://www.baidu.com/fdsaewq"));
	}
	
	public static String domainNameService(String domain) throws UnknownHostException
	{
		domain = domain.replaceAll("^[a-zA-Z]*://", "").replaceAll("/[\\S]*", "");
		return InetAddress.getByName(domain).getHostAddress();
	}
}
