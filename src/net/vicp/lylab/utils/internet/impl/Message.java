package net.vicp.lylab.utils.internet.impl;

import java.util.HashMap;
import java.util.Map;

import net.vicp.lylab.core.TranscodeObject;

/**
 * Generic Message Template
 * 
 * @author Young Lee
 * 
 */
public class Message extends TranscodeObject {
	private Integer code;
	private String message;
	private String key;
	private Map<String, Object> body;
	private Integer total;

	public Message() {
		code = -1;
		key = "Invalid";
		message = "Unknow";
		body = new HashMap<String, Object>();
		total = 0;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
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

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	
}
