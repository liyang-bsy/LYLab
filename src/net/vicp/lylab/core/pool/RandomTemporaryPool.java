package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.CoreDef;

/**
 * 
 * @author liyang
 *
 * 顺序化数据池
 *
 */
public class RandomTemporaryPool<T> extends SequencePool<T> {

	public RandomTemporaryPool() {
		this(CoreDef.DEFAULT_POOL_MAX_SIZE);
	}

	public RandomTemporaryPool(int maxSize) {
		super(maxSize);
	}
	
	@Override
	public T accessOne() {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.isEmpty())
				return null;
			T tmp = null;
			Long key = ((HashSet<Long>) keyContainer).iterator().next();
			tmp = removeFromContainer(key);
			keyContainer.remove(0);
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
				retList.add(removeFromContainer(iterator.next()));
				iterator.remove();
			}
			return retList;
		}
	}

}
