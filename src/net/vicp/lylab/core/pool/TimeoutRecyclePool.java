package net.vicp.lylab.core.pool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * 超时控制回收池
 * 
 * @author liyang
 *
 */
public class TimeoutRecyclePool<T> extends RecyclePool<T> implements Recyclable {
	protected Map<Long, Long> startTime;
	protected Long timeout;

	/**
	 * Default timeout is 2 minutes
	 */
	public TimeoutRecyclePool()
	{
		this(CoreDef.DEFAULT_CONTAINER_TIMEOUT, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(long timeout)
	{
		this(timeout, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(int maxSize)
	{
		this(CoreDef.DEFAULT_CONTAINER_TIMEOUT, maxSize);
	}
	
	public TimeoutRecyclePool(long timeout, int maxSize)
	{
		super(maxSize);
		startTime = new ConcurrentHashMap<Long, Long>();
		this.timeout = timeout;
		TimeoutController.addToWatch(this);
	}

	@Override
	public void clear() {
		synchronized (lock) {
			if (isClosed())
				return;
			super.clear();
			startTime.clear();
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			clear();
			super.close();
			startTime = null;
		}
	}
	
	@Override
	protected T getFromAvailableContainer(long objId) {
		safeCheck();
		if (availableSize() > 0)
		{
			T tmp = removeFromContainer(objId);
			if (tmp != null)
			{
				busyContainer.put(objId, tmp);
				startTime.put(objId, System.currentTimeMillis());
			}
			return tmp;
		}
		return null;
	}

	@Override
	public boolean recycle(long objId, boolean isBad) {
		synchronized (lock) {
			safeCheck();
			T tmp = busyContainer.remove(objId);
			if (tmp != null) {
				startTime.remove(objId);
				if(isBad)
					keyContainer.remove(objId);
				else
					addToContainer(tmp);
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean recycle(T item, boolean isBad) {
		synchronized (lock) {
			safeCheck();
			long objId = 0L;
			if (item instanceof BaseObject)
				objId = ((BaseObject) item).getObjectId();
			else {
				for (Long id : busyKeySet()) {
					if (busyContainer.get(id).equals(item)) {
						objId = id.longValue();
						break;
					}
				}
			}
			if(objId > 0)
				return recycle(objId, isBad);
			return false;
		}
	}
	
	public boolean recycle(long objId) {
		boolean ret = recycle(objId, false);
		if(ret)
			startTime.remove(objId);
		return ret;
	}
	
	@Override
	public void recycle() {
		synchronized (lock) {
			for (Long id : availableKeySet()) {
				T tmp = getFromContainer(id);
				if (tmp instanceof KeepAlive && ((KeepAlive) tmp).isDying()
						&& !((KeepAlive) tmp).isAlive())
					removeFromContainer(id);
			}
			for (Long id : startTime.keySet()) {
				long start = startTime.get(id);
				if (new Date().getTime() - start > timeout) {
					T tmp = busyContainer.get(id);
					if (tmp != null) {
						recycle(id, true);
						startTime.remove(id);
						try {
							if (tmp instanceof AutoCloseable) {
								((AutoCloseable) tmp).close();
							}
						} catch (Exception e) {
							throw new LYException("Recycle failed", e);
						}
//						busyContainer.remove(id);
//						keyContainer.remove(id);
					}
				}
			}
			if(keyContainer.size() != size())
				safeCheck();
		}
	}

	@Override
	public boolean isRecyclable() {
		safeCheck();
		return startTime.size() != 0;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

}
