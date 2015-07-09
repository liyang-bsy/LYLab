package net.vicp.lylab.utils.internet.impl;

import net.vicp.lylab.core.TranscodeObject;

/**
 * Generic Message Template
 * 
 * @author Young Lee
 * 
 */
public class SimpleConfirm extends TranscodeObject {
	private int code;
	
	public SimpleConfirm() {}

	public SimpleConfirm(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
