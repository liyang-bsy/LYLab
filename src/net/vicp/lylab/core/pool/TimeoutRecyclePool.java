package net.vicp.lylab.core.pool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDefine;
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
		this(2*CoreDefine.MINUTE, DEFAULT_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(long timeout)
	{
		this(timeout, DEFAULT_MAX_SIZE);
	}
	
	public TimeoutRecyclePool(int maxSize)
	{
		this(2*CoreDefine.MINUTE, maxSize);
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

//	@SuppressWarnings("unchecked")
	@Override
	public void recycle() {
		int sr = 0;
		int tr = 0;
		int ava=availableSize(), bus=busyContainer.size();
		for (Long id : startTime.keySet()) {
			Date start = startTime.get(id);
			if (new Date().getTime() - start.getTime() > timeout) {
				T tmp = busyContainer.remove(id);
				if (tmp != null) {
					keyContainer.remove(id);
					tr++;
//					while(size()<maxSize)
//						try {
//							addToContainer((T) tmp.getClass().newInstance());
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
				}
				startTime.remove(id);
				sr++;
			}
		}
		if(keyContainer.size() == size())
			safeCheck();
		System.out.println("\t回收报告:\tMax:" + (ava+bus) + "\tBava:" + ava + "\tBbus:" + bus +"\t\tava:" + availableSize() + "\tbus:" + busyContainer.size()
				+ (sr!=tr?"\t异常sr:" + sr + "\ttr:" + tr:""));
	}

	@Override
	public boolean isRecyclable() {
		safeCheck();
		return startTime.size() != 0;
	}

}
