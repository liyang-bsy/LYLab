package net.vicp.lylab.core.pool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * 
 * @author liyang
 *
 * 回收池
 *
 */
public class TimeoutRecyclePool<T> extends RecyclePool<T> implements Recyclable {
	protected Map<Long, Date> startTime;
	protected Long timeout;

	/**
	 * Default timeout is 2 minutes
	 */
	public TimeoutRecyclePool()
	{
		this(CoreDef.TWO * CoreDef.MINUTE, CoreDef.DEFAULT_POOL_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(long timeout)
	{
		this(timeout, CoreDef.DEFAULT_POOL_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(int maxSize)
	{
		this(2*CoreDef.MINUTE, maxSize);
	}
	
	public TimeoutRecyclePool(long timeout, int maxSize)
	{
		super(maxSize);
		startTime = new ConcurrentHashMap<Long, Date>();
		this.timeout = timeout;
		TimeoutController.addToWatch(this);
	}

	@Override
	public void clear() {
		synchronized (lock) {
			super.clear();
			startTime.clear();
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
				startTime.put(objId, new Date());
			}
			return tmp;
		}
		return null;
	}

	@Override
	public void recycle() {
		for (Long id : startTime.keySet()) {
			Date start = startTime.get(id);
			if (new Date().getTime() - start.getTime() > timeout) {
				try {
					T tmp = busyContainer.get(id);
					if (tmp != null) {
//						if (tmp instanceof Recyclable)
//							if(((Recyclable) tmp).isRecyclable())
//								((Recyclable) tmp).recycle();
//							else continue;
						if (tmp instanceof AutoCloseable)
							((AutoCloseable) tmp).close();
						busyContainer.remove(id);
						keyContainer.remove(id);
						startTime.remove(id);
					}
				} catch (Exception e) {
					throw new LYException("Recycle failed", e);
				}
			}
		}
		if(keyContainer.size() == size())
			safeCheck();
	}

	@Override
	public boolean isRecyclable() {
		safeCheck();
		return startTime.size() != 0;
	}

}
