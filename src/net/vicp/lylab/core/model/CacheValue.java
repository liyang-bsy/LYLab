package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

public class CacheValue extends CloneableBaseObject {
	
	long startTime;
	int validateTime;
	byte[] value;

	public CacheValue() { }

	public CacheValue(byte[] value, int validateTime) {
		if (validateTime == 0)
			startTime = Long.MAX_VALUE;
		else
			startTime = System.currentTimeMillis();
		this.validateTime = validateTime;
		this.value = value;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getValidateTime() {
		return validateTime;
	}

	public void setValidateTime(int validateTime) {
		this.validateTime = validateTime;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

}
