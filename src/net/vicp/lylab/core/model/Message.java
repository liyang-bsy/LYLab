package net.vicp.lylab.core.model;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class Message extends SimpleMessage {
	protected String token;
	protected int msgId;

	public Message() {
		super();
		token = "";
		msgId = 0;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	@Override
	public String toString() {
		return "Message [token=" + token + ", msgId=" + msgId + ", message="
				+ message + ", key=" + key + ", body=" + body + ", total="
				+ total + ", code=" + code + "]";
	}

}
