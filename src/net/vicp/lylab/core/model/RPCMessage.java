package net.vicp.lylab.core.model;

/**
 * Message template for RPC server, contains another Message to next server
 * 
 * @author Young Lee
 * 
 */
public class RPCMessage extends Message {
	protected String server;
	protected String rpcKey;
	protected boolean broadcast;
	
	public void copyBasicInfo(RPCMessage other) {
		super.copyBasicInfo(other);
		setServer(other.getServer());
		setRpcKey(other.getRpcKey());
		setBroadcast(other.isBroadcast());
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getRpcKey() {
		return rpcKey;
	}

	public void setRpcKey(String rpcKey) {
		this.rpcKey = rpcKey;
	}

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	@Override
	public String toString() {
		return "RPCMessage [server=" + server + ", broadcast=" + broadcast + ", rpcKey=" + rpcKey + ", token=" + token
				+ ", uuid=" + uuid + ", time=" + time + ", message=" + message + ", key=" + key + ", body=" + body
				+ ", code=" + code + "]";
	}

}
