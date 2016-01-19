package net.vicp.lylab.core.model;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class Call extends Message {
	protected String server;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}
