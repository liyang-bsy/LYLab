package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.LYException;

/**
 * 
 * @author liyang
 *
 * 抽象池
 *
 */
public abstract class AbstractPool<T> implements Pool<T> {
//	protected Lock lock = new ReentrantLock(true);
//	protected Condition full = lock.newCondition();

	static class Lock extends BaseObject { };
	protected Lock lock = new Lock();
	
	private Map<Long, T> availableContainer;
	protected Long idIndicator = 0L;
	public static final Integer DEFAULT_MAX_SIZE = 50;
	public Integer maxSize;

	public AbstractPool() {
		this(DEFAULT_MAX_SIZE);
	}

	public AbstractPool(Integer maxSize) {
		this.maxSize = (maxSize != null && maxSize > 0)? maxSize : DEFAULT_MAX_SIZE;
		this.availableContainer = new ConcurrentHashMap<Long, T>();
	}
	
	@Override
	public int size() {
		synchronized (lock) {
			return availableContainer.size();
		}
	}
	
	@Override
    public boolean isEmpty()
    {
		return availableContainer.isEmpty();
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
			this.notifyAll();
		}
	}

	protected T getFromContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		return availableContainer.get(objId);
	}

	protected T removeFromContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		T tmp = availableContainer.remove(objId);
		lock.notifyAll();
		return tmp;
	}
	
	protected Long addToContainer(T t) {
		Long savedId = null;
		if(t instanceof BaseObject)
		{
			while (!isClosed()) {
				Integer size = size();
				if (size >= maxSize) {
					try {
						lock.wait(CoreDefine.waitingThreshold);
						continue;
					} catch (InterruptedException e) {
						throw new LYException("Wait interrupted", e);
					}
				}
				if (size <= maxSize && size >= 0) {
					if (idIndicator == Long.MAX_VALUE)
						idIndicator = 0L;
					savedId = idIndicator;
					if(((BaseObject) t).getObjectId() == null)
					{
						try {
							((BaseObject) t).setObjectId(idIndicator.longValue());
						} catch (Exception e) { }
						availableContainer.put(idIndicator, t);
						idIndicator++;
					}
					else
					{
						availableContainer.put(((BaseObject) t).getObjectId(), t);
						savedId = ((BaseObject) t).getObjectId();
					}
					break;
				}
			}
		}
		return savedId;
	}
	
	public Set<Long> availableKeySet()
	{
		return availableContainer.keySet();
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

}
