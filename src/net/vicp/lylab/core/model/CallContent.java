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
	protected boolean broadcast;

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

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

}
