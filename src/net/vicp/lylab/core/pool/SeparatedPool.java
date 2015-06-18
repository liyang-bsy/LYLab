package net.vicp.lylab.core.pool;

import java.util.Map;
import java.util.Set;

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
		this(container, DEFAULT_MAX_SIZE);
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
	public synchronized boolean isClosed() {
		return (busyContainer == null || super.isClosed());
	}

	@Override
	public synchronized void clear() {
		if (isClosed())
			throw new LYException("This pool is already closed");
		busyContainer.clear();
		super.clear();
	}

	public synchronized boolean takeBack(Long objId) {
		if (isClosed() || objId == null)
			return false;
		T tmp = busyContainer.remove(objId);
		if(tmp != null)
		{
			addToContainer(tmp);
			return true;
		}
		return false;
	}

	@Override
	public synchronized T remove(Long objId) {
		if (isClosed() || objId == null)
			return null;
		T tmp = removeFromContainer(objId);
		// unless timeout, a busy item which shouldn't be removed.
//		if(tmp == null)
//			tmp = busyContainer.remove(objId);
		return tmp;
	}

	protected int availableSize() {
		return super.size();
	}

	protected synchronized T getFromAvailableContainer() {
		Long objId = availableKeySet().iterator().next();
		return getFromAvailableContainer(objId);
	}
	
	protected synchronized T getFromAvailableContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		if (availableSize() > 0)
		{
			T tmp = removeFromContainer(objId);
			if (tmp != null)
				busyContainer.put(objId, tmp);
			return tmp;
		}
		return null;
	}
	
	protected synchronized T getFromBusyContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		if (busyContainer.size() > 0)
		{
			T tmp = busyContainer.get(objId);
			return tmp;
		}
		return null;
	}

	public Set<Long> busyKeySet()
	{
		return busyContainer.keySet();
	}
	
}
