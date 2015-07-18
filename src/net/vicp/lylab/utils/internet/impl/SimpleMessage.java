package net.vicp.lylab.utils.internet.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic message template
 * 
 * @author Young Lee
 * 
 */
public class SimpleMessage extends SimpleConfirm {
	protected String message;
	protected String key;
	protected Map<String, Object> body;
	protected int total;

	public SimpleMessage() {
		super(-1);
		key = "Invalid";
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

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
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
