package net.vicp.lylab.core;

public class BaseObject {
	
	protected static class Lock { };
	protected Lock lock = new Lock();
	
	protected Long objectId;

	public Long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(Long objectId) {
		this.objectId = objectId;
		return this;
	}

}
