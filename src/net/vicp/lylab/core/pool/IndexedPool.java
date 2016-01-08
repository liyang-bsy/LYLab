package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYError;

/**
 * 线索化数据池
 * 
 * @author liyang
 * 
 */
public class IndexedPool<T extends BaseObject> extends AbstractPool<T> {
	protected volatile Collection<Long> keyContainer;

	public IndexedPool(Collection<Long> keyContainerType) {
		this(keyContainerType, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}

	public IndexedPool(Collection<Long> keyContainerType, int maxSize) {
		super(maxSize);
		keyContainer = keyContainerType;
	}
	
	public T view(long objId) {
		return getFromContainer(objId);
	}

	@Override
	public Long add(T t) {
		synchronized (lock) {
			safeCheck();
			Long id = addToContainer(t);
			if(id != null && !keyContainer.contains(id))
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
			keyContainer.remove(objId);
//			Iterator<Long> iterator = keyContainer.iterator();
//			while (iterator.hasNext()) {
//				if (!iterator.next().equals(objId))
//					continue;
//				iterator.remove();
//				break;
//			}
			return tmp;
		}
	}

	@Override
	public T accessOne() {
		synchronized (lock) {
			if (keyContainer.isEmpty())
				return null;
			Iterator<Long> iterator = keyContainer.iterator();
			long key = iterator.next();
			return getFromContainer(key);
		}
	}

	@Override
	public List<T> accessMany(int amount) {
		return accessMany(amount, false);
	}
	
	@Override
	public List<T> accessMany(int amount, boolean absolute) {
		synchronized (lock) {
			if (absolute && keyContainer.size() < amount)
				return null;
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
			super.clear();
			keyContainer.clear();
			safeCheck();
		}
	}

	protected void safeCheck()
	{
		synchronized (lock) {
			if(keyContainer.size() == size())
				return;
//			keyContainer.clear();
//			keyContainer.addAll(availableKeySet());
			throw new LYError("Pool maintainence failed! To continue use, please clear before next use"
					+ "\nkey list size is:" + keyContainer.size()
					+ "\ncontainer size is:" + size()
					+ "\nkey list is:" + keyContainer
					+ "\ncontainer is:" + size());
		}
	}

	@Override
	public Iterator<T> iterator() {
//		return new SequencePoolIterator(((LinkedList<Long>) keyContainer).listIterator(keyContainer.size()));
		return new SequencePoolIterator(keyContainer.iterator());
	}

	class SequencePoolIterator implements Iterator<T> {
		private Iterator<Long> iterator;
		private Long lastId = null;

//		public SequencePoolIterator(ListIterator<Long> iterator) {
		public SequencePoolIterator(Iterator<Long> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			lastId = iterator.next();
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
