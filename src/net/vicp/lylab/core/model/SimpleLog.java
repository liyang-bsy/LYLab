package net.vicp.lylab.core.model;

/**
 * Simple log data structure
 * 
 * @author Young Lee
 * 
 */
public class SimpleLog extends SimpleConfirm {
	private String ip;
	private String server;
	private String before;
	private String after;

	public SimpleLog() { }
	
	public SimpleLog(String ip, String server, String before, String after)
	{
		this.ip = ip;
		this.server = server;
		this.before = before;
		this.after = after;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	@Override
	public String toString() {
		return "SimpleLog [ip=" + ip + ", server=" + server + ", before="
				+ before + ", after=" + after + ", code=" + code + "]";
	}
	
}