package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.vicp.lylab.core.CoreDef;

/**
 * 线索化数据池
 * 
 * @author liyang
 * 
 */
public class IndexedPool<T> extends AbstractPool<T> {
	protected volatile Collection<Long> keyContainer;

	public IndexedPool(Collection<Long> keyContainerType) {
		this(keyContainerType, CoreDef.DEFAULT_POOL_MAX_SIZE);
	}

	public IndexedPool(Collection<Long> keyContainerType, int maxSize) {
		super(maxSize);
		keyContainer = keyContainerType;
	}
	
	public T viewById(long objId) {
		safeCheck();
		T tmp = getFromContainer(objId);
		return tmp;
	}

	@Override
	public Long add(T t) {
		synchronized (lock) {
			safeCheck();
			Long id = null;
			id = addToContainer(t);
			if(id != null && id >= 0)
				keyContainer.add(id);
			return id;
		}
	}

	@Override
	public T remove(long objId) {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.isEmpty())
				return null;
			T tmp = removeFromContainer(objId);
			Iterator<Long> iterator = keyContainer.iterator();
			while(iterator.hasNext())
			{
				if(!iterator.next().equals(objId))
					continue;
				iterator.remove();
				break;
			}
			return tmp;
		}
	}

	@Override
	public T accessOne() {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.isEmpty())
				return null;
			T tmp = null;
			Long key = null;
			key = keyContainer.iterator().next();
			tmp = getFromContainer(key);
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(int amount) {
		synchronized (lock) {
			safeCheck();
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator = keyContainer.iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				retList.add(getFromContainer(iterator.next()));
			}
			return retList;
		}
	}

	@Override
	public void clear() {
		synchronized (lock) {
			if (isClosed())
				return;
			super.clear();
			keyContainer.clear();
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			clear();
			super.close();
			keyContainer = null;
		}
	}

	protected void safeCheck()
	{
		synchronized (lock) {
			if(keyContainer.size() == size())
				return;
			keyContainer.clear();
			keyContainer.addAll(availableKeySet());
//			throw new LYError("Pool maintainence failed! To continue use, please clear before next use"
//					+ "\nkey list size is:" + keyContainer.size()
//					+ "\ncontainer size is:" + size());
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new SequencePoolIterator(((LinkedList<Long>) keyContainer).listIterator(keyContainer.size()));
	}

	class SequencePoolIterator implements Iterator<T> {
		private ListIterator<Long> iterator;
		private Long lastId;

		public SequencePoolIterator(ListIterator<Long> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasPrevious();
		}

		@Override
		public T next() {
			lastId = iterator.previous();
			return getFromContainer(lastId);
		}

		@Override
		public void remove() {
			if (lastId == null)
				return;
			synchronized (lock) {
				iterator.remove();
				IndexedPool.this.remove(lastId);
			}
		}
	}

}
