package net.vicp.lylab.core.pool.copy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYError;
import net.vicp.lylab.utils.Utils;

/**
 * 抽象的分离池
 * 
 * @author liyang
 *
 */
public class SeparatePool<T extends BaseObject> extends IndexedPool<T> {
	protected Map<Long, T> busyContainer;

	public SeparatePool() {
		this(CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}
	
	public SeparatePool(Integer maxSize) {
		super(new HashSet<Long>(), maxSize);
		busyContainer = new ConcurrentHashMap<Long, T>();
	}

	protected T getFromBusyContainer(long objId) {
		return busyContainer.get(objId);
	}
	
	@Override
	public T view(long objId) {
		safeCheck();
		T tmp = getFromContainer(objId);
		if(tmp == null)
			tmp = getFromBusyContainer(objId);
		return tmp;
	}

	@Override
	public int size() {
		return availableSize() + busySize();
	}
	
	protected int availableSize() {
		return super.size();
	}
	
	protected int busySize() {
		return busyContainer.size();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && busyContainer.isEmpty();
    }

	@Override
	public boolean isFull() {
		return size() == maxSize;
    }

	private void closeAllElement() {
		for(Long id:availableKeySet()) {
			T tmp = availableContainer.get(id);
			Utils.tryClose(tmp);
		}
		for(Long id:busyKeySet()) {
			T tmp = busyContainer.get(id);
			Utils.tryClose(tmp);
		}
	}

	@Override
	public void clear() {
		synchronized (lock) {
			closeAllElement();
			busyContainer.clear();
			super.clear();
			safeCheck();
		}
	}

	public Set<Long> busyKeySet() {
		return busyContainer.keySet();
	}

	public boolean recycle(T item) {
		return recycle(item.getObjectId());
	}

	public boolean recycle(T item, boolean isBad) {
		return recycle(item.getObjectId(), isBad);
	}

	public boolean recycle(long objId) {
		return recycle(objId, false);
	}

	public boolean recycle(long objId, boolean isBad) {
		synchronized (lock) {
			safeCheck();
			T tmp = busyContainer.remove(objId);
			if (tmp != null) {
				if (isBad) {
					keyContainer.remove(objId);
					Utils.tryClose(tmp);
				}
				else
					addToContainer(tmp);
				return true;
			}
			return false;
		}
	}

	@Override
	public T remove(long objId) {
		return remove(objId, true);
	}
	
	public T remove(long objId, boolean isAvailable) {
		synchronized (lock) {
			safeCheck();
			T tmp = null;
			if (isAvailable)
				tmp = removeFromContainer(objId);
			else
				tmp = busyContainer.remove(objId);
			if(tmp != null)
				keyContainer.remove(objId);
			return tmp;
		}
	}

	/**
	 * Force remove object, not matter this item is saved in available container or busy container.
	 * @param item to be removed
	 * @return
	 * removed item, null means not found
	 */
	protected T forceRemove(long objId) {
		synchronized (lock) {
			safeCheck();
			T tmp = null;
			if (objId > 0) {
				tmp = remove(objId, true);
				if(tmp == null)
					tmp = remove(objId, false);
			}
//			tryClose(tmp);
			return tmp;
		}
	}

	@Override
	public T accessOne() {
		synchronized (lock) {
			Iterator<Long> iterator = availableKeySet().iterator();
			if(!iterator.hasNext())
				return null;
			return accessOne(iterator.next());
		}
	}
	
	public T accessOne(long objId) {
		synchronized (lock) {
			safeCheck();
			if (availableSize() == 0)
				return null;
			T tmp = removeFromContainer(objId);
			if (tmp != null)
				busyContainer.put(objId, tmp);
			return tmp;
		}
	}
	
	@Override
	public List<T> accessMany(int amount, boolean absolute) {
		synchronized (lock) {
			if (absolute && keyContainer.size() < amount)
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
						retList.add(tmp);
					}
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
//			keyContainer.clear();
//			keyContainer.addAll(availableKeySet());
//			keyContainer.addAll(busyKeySet());
			throw new LYError("Pool maintainence failed! To continue use, please clear before next use"
					+ "\nkey list size is:" + keyContainer.size()
					+ "\ncontainer size is:" + size()
					+ "\nkey list is:" + keyContainer
					+ "\ncontainer is:" + availableContainer);
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
				SeparatePool.this.remove(lastId);
			}
		}
	}
	
}
