package net.vicp.lylab.core;

import net.vicp.lylab.core.exceptions.LYException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Every thing from BaseObject
 * 
 * @author Young
 *
 */
public class BaseObject {
	/**
	 * Every object has an id
	 */
	protected transient long objectId = 0L;

	public long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}
	
	/**
	 * Now every BaseObject may use this to log something
	 */
	protected transient static Log log = LogFactory.getLog(BaseObject.class);

	/**
	 * Inner lock
	 */
	protected static class Lock { };
	protected transient Lock lock = new Lock();

	protected void await(long timeout) {
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}

	protected void await() {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}
	
	protected void signal() {
		synchronized (lock) {
			lock.notify();
		}
	}

	protected void signalAll() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
}
