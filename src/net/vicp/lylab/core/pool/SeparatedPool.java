package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.LYException;

/**
 * 
 * @author liyang
 *
 * 抽象的分离池
 *
 */
public abstract class SeparatedPool<T> extends AbstractPool<T> {
	protected Map<Long, T> busyContainer;

	public SeparatedPool(Map<Long, T> container) {
		this(container, DEFAULT_maxSize);
	}

	public SeparatedPool(Map<Long, T> container, Integer maxSize) {
		super(maxSize);
		if(container == null)
			throw new LYException("Undefined pool type");
		busyContainer = container;
	}
	
	@Override
	public int size() {
		return availableSize() + busyContainer.size();
	}
	
	@Override
    public boolean isEmpty()
    {
		return super.isEmpty() && busyContainer.isEmpty();
    }

	@Override
	public void close() {
		if (busyContainer != null)
			busyContainer.clear();
		busyContainer = null;
		super.close();
	}

	@Override
	public boolean isClosed() {
		return (busyContainer == null || super.isClosed());
	}

	@Override
	public void clear() {
		try {
			if (isClosed())
				throw new LYException("This pool is already closed");
			lock.lock();
			busyContainer.clear();
			super.clear();
			full.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public boolean recycle(Long objId) {
		if (lock.tryLock()) {
			try {
				T tmp = busyContainer.remove(objId);
				if(tmp != null)
				{
					addToContainer(tmp);
					return true;
				}
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	@Override
	public T remove(Long objId) {
		if (isClosed() || objId == null)
			return null;
		try {
			if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
				try {
					T tmp = removeFromContainer(objId);
					if(tmp == null)
						tmp = busyContainer.remove(objId);
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

	protected int availableSize() {
		return super.size();
	}

	protected T getFromAvailableContainer() {
		try {
			if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
				Long objId = availableKeySet().iterator().next();
				return getFromAvailableContainer(objId);
			}
		} catch (InterruptedException e) {
			throw new LYException("Lock interrupted", e);
		}
		return null;
	}
	
	protected T getFromAvailableContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		if (availableSize() > 0)
		{
			try {
				if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
					try {
						T tmp = removeFromContainer(objId);
						if (tmp != null)
							busyContainer.put(objId, tmp);
						return tmp;
					} finally {
						lock.unlock();
					}
				}
			} catch (InterruptedException e) {
				throw new LYException("Lock interrupted", e);
			}
		}
		return null;
	}
	
	protected T getFromBusyContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		if (busyContainer.size() > 0)
		{
			try {
				if (lock.tryLock(CoreDefine.waitingThreshold, TimeUnit.MILLISECONDS)) {
					try {
						T tmp = busyContainer.get(objId);
						return tmp;
					} finally {
						lock.unlock();
					}
				}
			} catch (InterruptedException e) {
				throw new LYException("Lock interrupted", e);
			}
		}
		return null;
	}

	public Set<Long> busyKeySet()
	{
		return busyContainer.keySet();
	}
	
}
