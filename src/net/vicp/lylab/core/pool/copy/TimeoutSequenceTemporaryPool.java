package net.vicp.lylab.core.pool.copy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * 顺序化临时池
 * 
 * @author liyang
 *
 */
public class TimeoutSequenceTemporaryPool<T extends BaseObject> extends SequenceTemporaryPool<T> implements Recyclable {
	protected Map<Long, Long> startTime;
	protected Long timeout;

	/**
	 * Default timeout is 2 minutes
	 */
	public TimeoutSequenceTemporaryPool() {
		this(CoreDef.DEFAULT_CONTAINER_TIMEOUT, CoreDef.MASSIVE_CONTAINER_MAX_SIZE);
	}

	public TimeoutSequenceTemporaryPool(long timeout) {
		this(timeout, CoreDef.MASSIVE_CONTAINER_MAX_SIZE);
	}

	public TimeoutSequenceTemporaryPool(int maxSize) {
		this(CoreDef.DEFAULT_CONTAINER_TIMEOUT, maxSize);
	}

	public TimeoutSequenceTemporaryPool(long timeout, int maxSize) {
		super(maxSize);
		startTime = new ConcurrentHashMap<Long, Long>();
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
	public void close() {
		synchronized (lock) {
			clear();
			TimeoutController.removeFromWatch(this);
			startTime = null;
			keyContainer = null;
			availableContainer = null;
		}
	}

	/**
	 * Add to pool with index
	 * @param index -1 means addLast
	 * @param t
	 * @return
	 */
	@Override
	public Long add(int index, T t) {
		synchronized (lock) {
			safeCheck();
			Long id = addToContainer(t);
			if (id != null && !keyContainer.contains(id)) {
				if (index == -1)
					((LinkedList<Long>) keyContainer).addLast(id);
				else
					((LinkedList<Long>) keyContainer).add(index, id);
				startTime.put(id, System.currentTimeMillis());
			}
			return id;
		}
	}

	@Override
	public T accessOne() {
		synchronized (lock) {
			T tmp = super.accessOne();
			startTime.remove(tmp.getObjectId());
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(int amount, boolean absolute) {
		synchronized (lock) {
			safeCheck();
			if (absolute && keyContainer.size() < amount)
				return null;
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator = keyContainer.iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				retList.add(removeFromContainer(iterator.next()));
				// Keep balance
				iterator.remove();
			}
			return retList;
		}
	}

	@Override
	public void recycle() {
		recycle(1.0);
	}
	
	public void recycle(double rate) {
		synchronized (lock) {
			Iterator<Entry<Long, Long>> it = startTime.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Long, Long> entry = it.next();
				long start = entry.getValue();
				if (System.currentTimeMillis() - start > timeout) {
					remove(entry.getKey());
					it.remove();
				}
			}
//			for (Long id : startTime.keySet()) {
//				long start = startTime.get(id);
//				if (System.currentTimeMillis() - start > timeout * rate) {
//					availableContainer.remove(id);
//					keyContainer.remove(id);
//					startTime.remove(id);
//				}
//			}
//			if (keyContainer.size() == size())
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
	
	/**
	 * Cancel tolerant
	 */
//	@Override
//	protected void safeCheck() {
//		double amplify = 1.0;
//		if (1.0*size()/maxSize > 0.8) {
//			while(1.0*size()/maxSize > 0.5)
//			{
//				recycle(amplify);
//				amplify/=2;
//			}
//		}
//		super.safeCheck();
//	}

}
