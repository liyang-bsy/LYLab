package net.vicp.lylab.core.model;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.interfaces.Confirm;

/**
 * Simple confirm message
 * 
 * @author Young Lee
 * 
 */
public class SimpleConfirm extends BaseObject implements Confirm {
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
