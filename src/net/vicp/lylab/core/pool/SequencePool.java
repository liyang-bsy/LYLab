package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
		safeCheck();
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
		return tmp;
	}

	@Override
	public synchronized List<T> accessMany(Integer amount) {
		safeCheck();
		List<T> retList = new ArrayList<T>();
		Iterator<Long> iterator = keyContainer.iterator();
		for (int i = 0; !iterator.hasNext() && i < amount; i++) {
			retList.add(getFromContainer(iterator.next()));
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
			iterator.remove();
			removeFromContainer(lastId);
		}
	}
}
