package net.vicp.lylab.utils.internet.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class Message extends SimpleConfirm {
	protected String message;
	protected String key;
	protected String token;
	protected Map<String, Object> body;
	protected Integer total;

	public Message() {
		super(-1);
		key = "Invalid";
		token = "";
		message = "Unknow";
		body = new HashMap<String, Object>();
		total = 0;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	
	public void success()
	{
		this.setCode(0);
		this.setMessage("ok");
	}

	@Override
	public String toString() {
		return "Message [message=" + message + ", key=" + key + ", body="
				+ body + ", total=" + total + ", code=" + code + "]";
	}
	
}
