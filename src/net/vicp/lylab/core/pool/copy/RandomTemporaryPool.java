package net.vicp.lylab.core.pool.copy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;

/**
 * 随机读写临时池
 * 
 * @author liyang
 *
 */
public class RandomTemporaryPool<T extends BaseObject> extends SequencePool<T> {

	public RandomTemporaryPool() {
		this(CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
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
			Iterator<Long> iterator = keyContainer.iterator();
			long key = iterator.next();
			T tmp = removeFromContainer(key);
			// Keep balance
			iterator.remove();
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(int amount, boolean absolute) {
		synchronized (lock) {
			safeCheck();
			if (absolute && keyContainer.size() < amount)
				return null;
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator = keyContainer.iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				retList.add(removeFromContainer(iterator.next()));
				// Keep balance
				iterator.remove();
			}
			return retList;
		}
	}

}
