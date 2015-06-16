package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	protected Lock lock = new ReentrantLock(true);
	protected Condition full = lock.newCondition();

	private Map<Long, T> availableContainer;
	protected Long idIndicator = 0L;
	public static final Integer DEFAULT_maxSize = 16;
	public Integer maxSize;

	public AbstractPool() {
		this(DEFAULT_maxSize);
	}

	public AbstractPool(Integer maxSize) {
		this.maxSize = (maxSize != null && maxSize > 0)? maxSize : DEFAULT_maxSize;
		availableContainer = new ConcurrentHashMap<Long, T>();
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
	public void close() {
		try {
			lock.lock();
			if (availableContainer != null)
				availableContainer.clear();
			availableContainer = null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isClosed() {
		return availableContainer == null;
	}

	@Override
	public void clear() {
		try {
			if (isClosed())
				throw new LYException("This pool is already closed");
			lock.lock();
			availableContainer.clear();
		} finally {
			lock.unlock();
		}
	}

	protected T getFromContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		try {
			if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
				try {
					T tmp = availableContainer.get(objId);
					return tmp;
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new LYException("Lock interrupted", e);
		}
		return null;
	}

	protected T removeFromContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		try {
			if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
				try {
					T tmp = availableContainer.remove(objId);
					full.signalAll();
					return tmp;
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new LYException("Lock interrupted", e);
		}
		return null;
	}

	protected Long addToContainer(T t) {
		Long savedId = null;
		if(t instanceof BaseObject)
		{
			try {
				while (!isClosed() && lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
					try {
						Integer size = size();
						if (size >= maxSize) {
							try {
								full.await(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS);
								continue;
							} catch (InterruptedException e) {
								throw new LYException("Await interrupted", e);
							}
						}
						if (size <= maxSize && size >= 0) {
							if (idIndicator == Long.MAX_VALUE)
								idIndicator = 0L;
							savedId = idIndicator;
							try {
								((BaseObject) t).setObjectId(savedId.longValue());
							} catch (Exception e) { }
							availableContainer.put(savedId, t);
							idIndicator++;
							break;
						}
					} finally {
						lock.unlock();
					}
				}
			} catch (InterruptedException e) {
				throw new LYException("Lock interrupted", e);
			}
		}
		return savedId;
	}
	
	public Set<Long> availableKeySet()
	{
		return availableContainer.keySet();
	}

}
