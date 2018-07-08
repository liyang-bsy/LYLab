package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * 超时控制分离池
 * 
 * @author liyang
 *
 */
public class TimeoutRecyclePool<T extends BaseObject> extends SeparatePool<T> implements Recyclable {
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
			startTime.clear();
			super.clear();
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			clear();
			TimeoutController.removeFromWatch(this);
			startTime = null;
			keyContainer = null;
			availableContainer = null;
			busyContainer = null;
		}
	}
	
	@Override
	public T accessOne(long objId) {
		synchronized (lock) {
			safeCheck();
			if (availableSize() == 0)
				return null;
			T tmp = removeFromContainer(objId);
			if (tmp != null) {
				busyContainer.put(objId, tmp);
				startTime.put(objId, System.currentTimeMillis());
			}
			return tmp;
		}
	}
	
	@Override
	public List<T> accessMany(int amount, boolean absolute) {
		synchronized (lock) {
			if (absolute && availableSize() < amount)
				return null;
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator;
			iterator = availableKeySet().iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				try {
					long objId = iterator.next();
					T tmp = removeFromContainer(objId);
					if (tmp != null) {
						busyContainer.put(objId, tmp);
						startTime.put(objId, System.currentTimeMillis());
						retList.add(tmp);
					}
				} catch (Exception e) {
					continue;
				}
			}
			return retList;
		}
	}
	
//	@Override
//	protected T getFromContainer(long objId) {
//		super.getFromContainer(objId)
//		safeCheck();
//		if (availableSize() > 0)
//		{
//			T tmp = removeFromContainer(objId);
//			if (tmp != null) {
//				busyContainer.put(objId, tmp);
//				startTime.put(objId, System.currentTimeMillis());
//			}
//			return tmp;
//		}
//		return null;
//	}

	public boolean recycle(long objId, boolean isBad) {
		synchronized (lock) {
			boolean ret = super.recycle(objId, isBad);
			startTime.remove(objId);
			return ret;
		}
	}

	@Override
	public void recycle() {
		synchronized (lock) {
			for (Long id : availableKeySet()) {
				T tmp = getFromContainer(id);
				if (tmp instanceof KeepAlive && ((KeepAlive) tmp).isOutdated() && !((KeepAlive) tmp).isAlive()) {
					removeFromContainer(id);
					Utils.tryClose(tmp);
				}
			}
			Iterator<Entry<Long, Long>> it = startTime.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Long, Long> entry = it.next();
				long start = entry.getValue();
				if (System.currentTimeMillis() - start > timeout && busyContainer.containsKey(entry.getKey())) {
					super.recycle(entry.getKey(), true);
					it.remove();
				}
			}
			safeCheck();
		}
	}

	@Override
	public boolean isRecyclable() {
		return size() != 0;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

}
