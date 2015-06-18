package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.LYError;

/**
 * 
 * @author liyang
 *
 * 顺序化数据池
 *
 */
public class SequencePool<T> extends AbstractPool<T> {
	protected List<Long> keyContainer = new LinkedList<Long>();

	public SequencePool() {
		this(DEFAULT_MAX_SIZE);
	}

	public SequencePool(Integer maxSize) {
		super(maxSize);
	}

	@Override
	public Long add(T t) {
		return add(0, t);
	}

	public synchronized Long add(Integer index, T t) {
		Long id = null;
		id = addToContainer(t);
		if(id != null && id >= 0)
			keyContainer.add(index, id);
		return id;
	}

	@Override
	public synchronized T remove(Long objId) {
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

	@Override
	public synchronized T accessOne() {
		safeCheck();
		if (keyContainer.isEmpty())
			return null;
		T tmp = null;
		Long key = keyContainer.get(0);
		tmp = getFromContainer(key);
		keyContainer.remove(0);
		return tmp;
	}

	@Override
	public synchronized List<T> accessMany(Integer amount) {
		safeCheck();
		List<T> retList = new ArrayList<T>();
		Iterator<Long> iterator = keyContainer.iterator();
		for (int i = 0; !iterator.hasNext() && i < amount; i++) {
			retList.add(getFromContainer(iterator.next()));
			iterator.remove();
		}
		return retList;
	}

	@Override
	public synchronized void clear() {
		if(!isClosed())
		{
			keyContainer.clear();
			super.clear();
		}
	}

	@Override
	public synchronized void close() {
		if (keyContainer != null)
			keyContainer.clear();
		keyContainer = null;
		super.close();
	}

	protected synchronized void safeCheck()
	{
		if(keyContainer.size() != size())
			throw new LYError("Pool maintainence failed! To continue use, please clear before next use"
					+ "\nkey list size is:" + keyContainer.size()
					+ "\ncontainer size is:" + size());
	}

	@Override
	public Iterator<T> iterator() {
		return new SequencePoolIterator(keyContainer.iterator());
	}

	class SequencePoolIterator implements Iterator<T> {
		private Iterator<Long> iterator;
		private Long lastId;

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
			iterator.remove();
			removeFromContainer(lastId);
		}
	}
}
