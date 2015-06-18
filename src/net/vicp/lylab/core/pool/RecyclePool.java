package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 
 * @author liyang
 *
 * 回收池
 *
 */
public class RecyclePool<T> extends SeparatedPool<T> {
	private Set<Long> keySet = new HashSet<Long>();

	public RecyclePool()
	{
		this(new WeakHashMap<Long, T>(), DEFAULT_MAX_SIZE);
	}
	
	public RecyclePool(Integer maxSize)
	{
		this(new WeakHashMap<Long, T>(), maxSize);
	}
	
	public RecyclePool(Map<Long, T> container, Integer maxSize)
	{
		super(new WeakHashMap<Long, T>(), maxSize);
	}

	public synchronized boolean recycle(Long objId) {
		return takeBack(objId);
	}

	@Override
	public synchronized Long add(T t) {
		return add(0, t);
	}

	public synchronized Long add(Integer index, T t) {
		if (keySet.size() >= maxSize)
			return null;
		Long id = null;
		id = addToContainer(t);
		if(id != null && id >= 0)
			keySet.add(id);
		return id;
	}

	@Override
	public synchronized T accessOne()
	{
		return accessOne(true);
	}
	
	public synchronized T accessOne(boolean available) {
		if (keySet.isEmpty())
			return null;
		T tmp = null;
		try {
			if(available) tmp = getFromAvailableContainer();
		} catch (Exception e) { }
		return tmp;
	}

	@Override
	public synchronized List<T> accessMany(Integer amount)
	{
		return accessMany(amount, false);
	}

	public synchronized List<T> accessMany(Integer amount, boolean available) {
		List<T> retList = new ArrayList<T>();
		Iterator<Long> iterator = keySet.iterator();
		for (int i = 0; !iterator.hasNext() && i < amount; i++) {
			try {
				Long key = iterator.next();
				T tmp = null;
				if(available) tmp = getFromAvailableContainer(key);
				else tmp = getFromBusyContainer(key);
				if(tmp!=null) retList.add(tmp);
				iterator.remove();
			} catch (Exception e) {
				continue;
			}
		}
		return retList;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new RecyclePoolIterator(keySet.iterator());
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
			iterator.remove();
			RecyclePool.this.remove(lastId);
		}
	}

}
