package net.vicp.lylab.core.model;

public class ClientIdentity extends Pair<String, Integer> {
	
	public String getHostName() {
		return getLeft();
	}

	public void setHostName(String hostName) {
		setLeft(hostName);
	}

	public Integer getIp() {
		return getRight();
	}

	public void setIp(Integer ip) {
		setRight(ip);
	}

}
