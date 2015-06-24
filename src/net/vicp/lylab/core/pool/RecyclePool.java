package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;

/**
 * 
 * @author liyang
 *
 * 抽象的分离池
 *
 */
public class RecyclePool<T> extends AbstractPool<T> {
	protected Map<Long, T> busyContainer;
	protected Set<Long> keyContainer = new HashSet<Long>();

	public RecyclePool() {
		this(new ConcurrentHashMap<Long, T>(), DEFAULT_MAX_SIZE);
	}
	
	public RecyclePool(Map<Long, T> container) {
		this(container, DEFAULT_MAX_SIZE);
	}
	
	public RecyclePool(Integer maxSize) {
		this(new ConcurrentHashMap<Long, T>(), maxSize);
	}

	public RecyclePool(Map<Long, T> container, Integer maxSize) {
		super(maxSize);
		if(container == null)
			throw new LYException("Undefined pool type");
		busyContainer = container;
	}
	
	@Override
	public int size() {
		return availableSize() + busyContainer.size();
	}
	
	protected int availableSize() {
		return super.size();
	}

	@Override
    public boolean isEmpty()
    {
		return super.isEmpty() && busyContainer.isEmpty();
    }

	@Override
	public void close() {
		synchronized (lock) {
			if (busyContainer != null)
				busyContainer.clear();
			busyContainer = null;
			super.close();
		}
	}

	@Override
	public boolean isClosed() {
		synchronized (lock) {
			return (busyContainer == null || super.isClosed());
		}
	}

	@Override
	public void clear() {
		synchronized (lock) {
			if (isClosed())
				throw new LYException("This pool is already closed");
			busyContainer.clear();
			super.clear();
		}
	}

	public Set<Long> busyKeySet()
	{
		return busyContainer.keySet();
	}

	public boolean recycle(Long objId) {
		synchronized (lock) {
			if (isClosed() || objId == null)
				return false;
			T tmp = busyContainer.remove(objId);
			if(tmp != null)
			{
				if (tmp instanceof Recyclable) {
					((Recyclable) tmp).recycle();
				}
				addToContainer(tmp);
				return true;
			}
			return false;
		}
	}

	@Override
	public Long add(T t) {
		return add(0, t);
	}

	public Long add(Integer index, T t) {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.size() >= maxSize)
				return null;
			Long id = null;
			id = addToContainer(t);
			if(id != null && id >= 0)
				keyContainer.add(id);
			return id;
		}
	}

	@Override
	public T remove(Long objId) {
		synchronized (lock) {
			safeCheck();
			if (isClosed() || objId == null)
				return null;
			T tmp = removeFromContainer(objId);
			if(tmp != null)
				keyContainer.remove(objId);
			return tmp;
		}
	}

	protected T getFromAvailableContainer() {
		Long objId = availableKeySet().iterator().next();
		return getFromAvailableContainer(objId);
	}
	
	protected T getFromAvailableContainer(Long objId) {
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

	protected T getFromBusyContainer() {
		Long objId = busyKeySet().iterator().next();
		return getFromBusyContainer(objId);
	}
	
	protected T getFromBusyContainer(Long objId) {
		if (isClosed() || objId == null)
			return null;
		if (busyContainer.size() > 0)
		{
			T tmp = busyContainer.get(objId);
			return tmp;
		}
		return null;
	}

	@Override
	public T accessOne()
	{
		return accessOne(true);
	}
	
	public T accessOne(boolean available) {
		safeCheck();
		synchronized (lock) {
			if (keyContainer.isEmpty())
				return null;
			T tmp = null;
			try {
				if(available) tmp = getFromAvailableContainer();
				else tmp = getFromBusyContainer();
			} catch (Exception e) { }
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(Integer amount)
	{
		return accessMany(amount, false);
	}

	public List<T> accessMany(Integer amount, boolean available) {
		safeCheck();
		synchronized (lock) {
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator;
			if(available) iterator = availableKeySet().iterator();
			else iterator = busyKeySet().iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				try {
					Long key = iterator.next();
					T tmp = null;
					if(available) tmp = getFromAvailableContainer(key);
					else tmp = getFromBusyContainer(key);
					if(tmp!=null)
					{
						retList.add(tmp);
					}
					iterator.remove();
				} catch (Exception e) {
					continue;
				}
			}
			return retList;
		}
	}

	protected void safeCheck()
	{
		synchronized (lock) {
			if(keyContainer.size() == size())
				return;
			keyContainer.clear();
			keyContainer.addAll(availableKeySet());
			keyContainer.addAll(busyKeySet());
//			if(keyContainer.size() != size())
//				throw new LYError("Pool maintainence failed! To continue use, please clear before next use"
//						+ "\nkey list size is:" + keyContainer.size()
//						+ "\ncontainer size is:" + size());
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return new RecyclePoolIterator(keyContainer.iterator());
	}

	class RecyclePoolIterator implements Iterator<T> {
		private Iterator<Long> iterator;
		private Long lastId;

		public RecyclePoolIterator(Iterator<Long> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			lastId = iterator.next();
			T tmp = getFromContainer(lastId);
			if(tmp == null)
				tmp = getFromBusyContainer(lastId);
			return tmp;
		}

		@Override
		public void remove() {
			if (lastId == null)
				return;
			synchronized (lock) {
				iterator.remove();
				RecyclePool.this.remove(lastId);
			}
		}
	}
	
}
