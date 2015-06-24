package net.vicp.lylab.core.pool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.Recyclable;

/**
 * 
 * @author liyang
 *
 * 回收池
 *
 */
public class TimeoutRecyclePool<T> extends RecyclePool<T> implements Recyclable {
	protected Map<Long, Date> startTime;
	
	public TimeoutRecyclePool()
	{
		this(new ConcurrentHashMap<Long, T>(), DEFAULT_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(Integer maxSize)
	{
		this(new ConcurrentHashMap<Long, T>(), maxSize);
	}
	
	public TimeoutRecyclePool(Map<Long, T> container, Integer maxSize)
	{
		super(new ConcurrentHashMap<Long, T>(), maxSize);
	}

	@Override
	public void recycle() {
		
	}

	@Override
	public boolean isRecyclable() {
		return true;
	}

}
