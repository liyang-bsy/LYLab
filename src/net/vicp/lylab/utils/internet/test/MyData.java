package net.vicp.lylab.utils.internet.test;

import net.vicp.lylab.core.TranscodeProtocol;

public class MyData extends TranscodeProtocol {
	String value;

	public MyData() { }
	
	public MyData(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "value=" + value;
	}

}
