package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDef;

/**
 * 抽象池
 * 
 * @author liyang
 *
 */
public abstract class AbstractPool<T extends BaseObject> extends CloneableBaseObject implements Pool<T> {
	
	protected Map<Long, T> availableContainer;
	protected long idIndicator = 1L;
	protected int maxSize;

	public AbstractPool() {
		this(CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}

	public AbstractPool(int maxSize) {
		this.maxSize = ((maxSize > 0)? maxSize : CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
		this.availableContainer = new ConcurrentHashMap<Long, T>();
	}
	
	@Override
	public int size() {
		return availableContainer.size();
	}

	@Override
	public boolean isEmpty() {
		return availableContainer.isEmpty();
	}

	@Override
	public boolean isFull() {
		return availableContainer.size() == maxSize;
    }

	@Override
	public void clear() {
		synchronized (lock) {
			availableContainer.clear();
			lock.notifyAll();
		}
	}

	protected T getFromContainer(long objId) {
		return availableContainer.get(objId);
	}

	protected T removeFromContainer(long objId) {
		T tmp = availableContainer.remove(objId);
		lock.notifyAll();
		return tmp;
	}
	
	/**
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	protected Long addToContainer(T t) {
		synchronized (lock) {
			if (size() >= maxSize)
				return null;
//			do {
//				if (idIndicator == Long.MAX_VALUE)
//					idIndicator = 1L;
//					idIndicator++;
//			} while(!availableContainer.containsKey(idIndicator));
			long savedId = idIndicator;
			if (t.getObjectId() <= 0L) {
				t.setObjectId(savedId);
				availableContainer.put(savedId, t);
			} else {
				availableContainer.put(t.getObjectId(), t);
				savedId = t.getObjectId();
			}
			return savedId;
		}
	}
	
	public Set<Long> availableKeySet() {
		return availableContainer.keySet();
	}

	@Override
	public int getMaxSize() {
		return maxSize;
	}

	@Override
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

}
