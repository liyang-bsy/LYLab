package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * 
 * @author liyang
 *
 * 回收池
 *
 */
public abstract class RecyclePool<T> extends SeparatedPool<T> {
	private List<Long> keyList = new LinkedList<Long>();

	public RecyclePool()
	{
		super(new WeakHashMap<Long, T>());
	}
	
	public RecyclePool(Integer maxSize)
	{
		super(new WeakHashMap<Long, T>(), maxSize);
	}

	@Override
	public synchronized Long add(T t) {
		return add(0, t);
	}

	public synchronized Long add(Integer index, T t) {
		Long id = null;
		id = addToContainer(t);
		if(id != null && id >= 0)
			keyList.add(index, id);
		return id;
	}

	@Override
	public synchronized T accessOne()
	{
		return accessOne(true);
	}
	
	public synchronized T accessOne(boolean available) {
		if (keyList.isEmpty())
			return null;
		T tmp = null;
		try {
			Long key = keyList.get(0);
			if(available) tmp = getFromAvailableContainer(key);
			else tmp = getFromBusyContainer(key);
			keyList.remove(0);
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
		Iterator<Long> iterator = keyList.iterator();
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

}
