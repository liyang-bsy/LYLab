package net.vicp.lylab.core.model;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class Message extends SimpleMessage {
	protected String token;
	protected String uuid;
	protected Long time;

	public Message() {
		super();
		token = "";
		uuid = "";
		time = System.currentTimeMillis();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Message [token=" + token + ", uuid=" + uuid + ", time="
				+ time + ", message=" + message + ", key=" + key + ", body="
				+ body + ", code=" + code + "]";
	}

}
