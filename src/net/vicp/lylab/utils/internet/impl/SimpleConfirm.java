package net.vicp.lylab.utils.internet.impl;

import net.vicp.lylab.core.BaseObject;

/**
 * Simple confirm message
 * 
 * @author Young Lee
 * 
 */
public class SimpleConfirm extends BaseObject {
	protected int code;
	
	public SimpleConfirm() { }

	public SimpleConfirm(Integer code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
