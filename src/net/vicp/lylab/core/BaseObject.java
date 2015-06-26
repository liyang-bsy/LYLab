package net.vicp.lylab.core;

public class BaseObject {
	
	protected static class Lock { };
	protected Lock lock = new Lock();
	
	protected long objectId = 0L;

	public long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}

}
