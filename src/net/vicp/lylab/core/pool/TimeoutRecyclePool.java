package net.vicp.lylab.core.pool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.interfaces.Recyclable;

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
		this(new ConcurrentHashMap<Long, T>(), 2*CoreDefine.MINUTE, DEFAULT_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(Long timeout)
	{
		this(new ConcurrentHashMap<Long, T>(), timeout, DEFAULT_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(Integer maxSize)
	{
		this(new ConcurrentHashMap<Long, T>(), 2*CoreDefine.MINUTE, maxSize);
	}
	
	public TimeoutRecyclePool(Map<Long, T> container, Long timeout, Integer maxSize)
	{
		super(new ConcurrentHashMap<Long, T>(), maxSize);
	}

	@Override
	public Long add(T t) {
		return add(0, t);
	}

	@Override
	public Long add(Integer index, T t) {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.size() >= maxSize)
				return null;
			Long id = null;
			id = addToContainer(t);
			if (id != null && id >= 0) {
				startTime.put(id, new Date());
				keyContainer.add(id);
			}
			return id;
		}
	}

	@Override
	public void recycle() {
		for (Long id : startTime.keySet()) {
			Date start = startTime.get(id);
			if (new Date().getTime() - start.getTime() > timeout) {
				if (remove(id) == null) {
					busyContainer.remove(id);
					keyContainer.remove(id);
				}
				startTime.remove(id);
			}
		}
	}

	@Override
	public boolean isRecyclable() {
		return true;
	}

}
