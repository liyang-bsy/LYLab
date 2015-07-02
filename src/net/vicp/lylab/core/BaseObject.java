package net.vicp.lylab.core;

import net.vicp.lylab.core.exception.LYException;

public class BaseObject {

	protected long objectId = 0L;

	public long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}

	protected static class Lock { };
	protected Lock lock = new Lock();

	protected void await(long timeout)
	{
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}

	protected void await()
	{
		synchronized (lock) {
			try {
				lock.wait();
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}
	
	protected void signal()
	{
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
}
