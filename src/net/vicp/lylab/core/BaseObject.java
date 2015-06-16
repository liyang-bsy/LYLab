package net.vicp.lylab.core;

public class BaseObject {
	protected volatile long objectId;

	public long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}

}
