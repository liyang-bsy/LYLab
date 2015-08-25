package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;

/**
 * 抽象池
 * 
 * @author liyang
 *
 */
public abstract class AbstractPool<T> extends CloneableBaseObject implements Pool<T> {
	
	protected Map<Long, T> availableContainer;
	protected Long idIndicator = 1L;
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
    public boolean isEmpty()
    {
		return availableContainer.isEmpty();
    }

	@Override
    public boolean isFull()
    {
		return availableContainer.size() == maxSize;
    }

	@Override
	public void close() {
		synchronized (lock) {
			if (availableContainer != null)
				availableContainer.clear();
			availableContainer = null;
		}
	}

	@Override
	public boolean isClosed() {
		return availableContainer == null;
	}

	@Override
	public void clear() {
		synchronized (lock) {
			if (isClosed())
				throw new LYException("This pool is already closed");
			availableContainer.clear();
			lock.notifyAll();
		}
	}

	protected T getFromContainer(long objId) {
		if (isClosed())
			return null;
		return availableContainer.get(objId);
	}

	protected T removeFromContainer(long objId) {
		if (isClosed())
			return null;
		T tmp = availableContainer.remove(objId);
		lock.notifyAll();
		return tmp;
	}
	
	protected Long addToContainer(T t) {
		synchronized (lock) {
			if (isClosed() || size() >= maxSize)
				return null;
			Long savedId = null;
			do {
				if (idIndicator == Long.MAX_VALUE)
					idIndicator = 1L;
				idIndicator++;
			} while(availableContainer.get(idIndicator) != null);
			savedId = idIndicator;
			if (t instanceof BaseObject) {
				Long id = ((BaseObject) t).getObjectId();
				if (id == null || id.longValue() <= 0L) {
					((BaseObject) t).setObjectId(savedId.longValue());
					availableContainer.put(savedId, t);
				} else {
					availableContainer.put(((BaseObject) t).getObjectId(), t);
					savedId = ((BaseObject) t).getObjectId();
				}
			} else
				availableContainer.put(savedId, t);
			return savedId;
		}
	}
	
	public Set<Long> availableKeySet()
	{
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
