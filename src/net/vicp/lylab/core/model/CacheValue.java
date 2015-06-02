package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

public class CacheValue extends CloneableBaseObject {
	long startTime;
	long validateTime;
	byte[] value;

	public CacheValue(byte[] value, long validateTime) {
		startTime = System.currentTimeMillis();
		this.validateTime = validateTime;
		this.value = value;
	}
	
	public CacheValue() {
		startTime = System.currentTimeMillis();
		this.validateTime = 0L;
		this.value = null;
	}

	public void renewStartTime()
	{
		startTime = System.currentTimeMillis();
	}
	
	public int size() {
		return value.length;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getValidateTime() {
		return validateTime;
	}

	public void setValidateTime(long validateTime) {
		this.validateTime = validateTime;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

}
