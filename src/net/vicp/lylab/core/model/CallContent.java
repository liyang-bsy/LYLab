package net.vicp.lylab.core.model;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class CallContent extends Message {
	protected String server;
	protected String procedure;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getProcedure() {
		return procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

}
