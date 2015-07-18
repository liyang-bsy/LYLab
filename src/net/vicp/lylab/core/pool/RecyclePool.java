package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.Utils;

/**
 * 抽象的分离池
 * 
 * @author liyang
 *
 */
public class RecyclePool<T> extends IndexedPool<T> {
	protected Map<Long, T> busyContainer;

	public RecyclePool() {
		this(CoreDef.DEFAULT_POOL_MAX_SIZE);
	}
	
	public RecyclePool(Integer maxSize) {
		super(new HashSet<Long>(), maxSize);
		busyContainer = new ConcurrentHashMap<Long, T>();
	}

	public T viewById(long objId) {
		safeCheck();
		T tmp = getFromContainer(objId);
		if(tmp == null)
			tmp = getFromBusyContainer(objId);
		return tmp;
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
			clear();
			super.close();
			busyContainer = null;
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
				return;
			for(Long id:availableKeySet())
			{
				T tmp = availableContainer.get(id);
				if(tmp instanceof AutoCloseable)
					try {
						((AutoCloseable) tmp).close();
					} catch (Exception e) {
						log.error(Utils.getStringFromException(e));
					}
			}
			for(Long id:busyKeySet())
			{
				T tmp = busyContainer.get(id);
				if(tmp instanceof AutoCloseable)
					try {
						((AutoCloseable) tmp).close();
					} catch (Exception e) {
						log.error(Utils.getStringFromException(e));
					}
			}
			super.clear();
			keyContainer.clear();
			busyContainer.clear();
		}
	}

	public Set<Long> busyKeySet()
	{
		return busyContainer.keySet();
	}

	public boolean recycle(long objId) {
		synchronized (lock) {
			safeCheck();
			T tmp = busyContainer.remove(objId);
			if (tmp != null) {
				addToContainer(tmp);
				return true;
			}
			return false;
		}
	}

	public boolean recycle(T item) {
		synchronized (lock) {
			safeCheck();
			long objId = 0L;
			if (item instanceof BaseObject)
				objId = ((BaseObject) item).getObjectId();
			else {
				for (Long id : busyKeySet()) {
					if (busyContainer.get(id).equals(item)) {
						objId = id.longValue();
						break;
					}
				}
			}
			if (objId > 0) {
				T tmp = busyContainer.remove(objId);
				if (tmp != null) {
					addToContainer(tmp);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public T remove(long objId) {
		synchronized (lock) {
			safeCheck();
			T tmp = removeFromContainer(objId);
			if(tmp != null)
				keyContainer.remove(objId);
			return tmp;
		}
	}

	/**
	 * Search and remove from both available container and busy container.
	 * @param item to be removed
	 * @return
	 * removed item, null means not found
	 */
	protected T searchAndRemove(T item) {
		synchronized (lock) {
			if (isClosed() && item == null)
				return null;
			safeCheck();
			long objId = 0L;
			if (item instanceof BaseObject)
				objId = ((BaseObject) item).getObjectId();
			else {
				for (Long id : availableKeySet()) {
					if (availableContainer.get(id).equals(item)) {
						objId = id.longValue();
						break;
					}
				}
				if (objId == 0)
					for (Long id : busyKeySet()) {
						if (busyContainer.get(id).equals(item)) {
							objId = id.longValue();
							break;
						}
					}
			}
			T tmp = null;
			if (objId > 0) {
				tmp = removeFromContainer(objId);
				if (tmp == null)
					tmp = busyContainer.remove(objId);
				if (tmp != null)
					keyContainer.remove(objId);
			}
			return tmp;
		}
	}

	protected T getFromAvailableContainer() {
		Iterator<Long> iterator = availableKeySet().iterator();
		if(!iterator.hasNext())
			return null;
		return getFromAvailableContainer(iterator.next());
	}
	
	protected T getFromAvailableContainer(long objId) {
		safeCheck();
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
	
	protected T getFromBusyContainer(long objId) {
		safeCheck();
		if (busyContainer.size() <= 0)
			return null;
		return busyContainer.get(objId);
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
			if(available) tmp = getFromAvailableContainer();
			else tmp = getFromBusyContainer();
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(int amount)
	{
		return accessMany(amount, false);
	}

	public List<T> accessMany(int amount, boolean available) {
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
					if(tmp!=null) {
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
			if(isClosed() || keyContainer.size() == size())
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
		safeCheck();
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
