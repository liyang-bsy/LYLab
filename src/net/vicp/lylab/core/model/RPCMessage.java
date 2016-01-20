package net.vicp.lylab.core.model;

/**
 * Message template for RPC server, contains another Message to next server
 * 
 * @author Young Lee
 * 
 */
public class RPCMessage extends Message {
	protected String server;
//	protected String procedure;
	protected boolean broadcast;
	protected Message rpcReq;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

//	public String getProcedure() {
//		return procedure;
//	}
//
//	public void setProcedure(String procedure) {
//		this.procedure = procedure;
//	}

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	public Message getRpcReq() {
		return rpcReq;
	}

	public void setRpcReq(Message rpcReq) {
		this.rpcReq = rpcReq;
	}

}
