package net.vicp.lylab.core.model;

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

	public SimpleMessage() {
		super(-1);
		key = "Invalid";
		message = "Unknow";
		body = new HashMap<String, Object>();
	}
	
	public void copyBasicInfo(SimpleMessage other) {
		setKey(other.getKey());
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

	public void success() {
		this.setCode(0);
		this.setMessage("ok");
	}

	public void fail(String msg) {
		this.setCode(1);
		this.setMessage(msg);
	}
	
	public void fail(int code, String msg) {
		this.setCode(1);
		this.setMessage(msg);
	}

	@Override
	public String toString() {
		return "Message [message=" + message + ", key=" + key + ", body="
				+ body + ", code=" + code + "]";
	}

}
