package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

/**
 * This object contains information about a remote connection
 * 
 * @author liyang
 *
 */
public class InetAddr extends CloneableBaseObject {
	private String ip;
	private int port;

	public InetAddr(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public static InetAddr fromInetAddr(String ip, int port) {
		return new InetAddr(ip, port);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InetAddr other = (InetAddr) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InetAddr [ip=" + ip + ", port=" + port + "]";
	}

}
