package net.vicp.lylab.core.model;

import java.util.UUID;

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
		uuid = UUID.randomUUID().toString();
		time = System.currentTimeMillis();
	}
	
	public void copyBasicInfo(Message other) {
		super.copyBasicInfo(other);
		setToken(other.getToken());
		setUuid(other.getUuid());
		setTime(other.getTime());
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
