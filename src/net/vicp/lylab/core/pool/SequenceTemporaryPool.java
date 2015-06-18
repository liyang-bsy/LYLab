package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author liyang
 *
 * 顺序化数据池
 *
 */
public class SequenceTemporaryPool<T> extends SequencePool<T> {

	public SequenceTemporaryPool() {
		this(DEFAULT_MAX_SIZE);
	}

	public SequenceTemporaryPool(Integer maxSize) {
		super(maxSize);
	}
	
	public T viewOne(Long objId) {
		safeCheck();
		T tmp = null;
		try {
			tmp = getFromContainer(objId);
		} catch (Exception e) { }
		return tmp;
	}
	
	@Override
	public synchronized T accessOne() {
		safeCheck();
		if (keyContainer.isEmpty())
			return null;
		T tmp = null;
		Long key = keyContainer.get(0);
		tmp = removeFromContainer(key);
		keyContainer.remove(0);
		return tmp;
	}

	@Override
	public synchronized List<T> accessMany(Integer amount) {
		safeCheck();
		List<T> retList = new ArrayList<T>();
		Iterator<Long> iterator = keyContainer.iterator();
		for (int i = 0; !iterator.hasNext() && i < amount; i++) {
			retList.add(removeFromContainer(iterator.next()));
			iterator.remove();
		}
		return retList;
	}

}
