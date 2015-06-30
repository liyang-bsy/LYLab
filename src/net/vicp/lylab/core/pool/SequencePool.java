package net.vicp.lylab.core.pool;

import java.util.LinkedList;

import net.vicp.lylab.core.CoreDef;

/**
 * 顺序化数据池
 * 
 * @author liyang
 *
 */
public class SequencePool<T> extends IndexedPool<T> {

	public SequencePool() {
		this(CoreDef.DEFAULT_POOL_MAX_SIZE);
	}

	public SequencePool(int maxSize) {
		super(new LinkedList<Long>(), maxSize);
	}

	public Long add(int index, T t) {
		synchronized (lock) {
			safeCheck();
			Long id = null;
			id = addToContainer(t);
			if(id != null && id >= 0)
				((LinkedList<Long>) keyContainer).add(index, id);
			return id;
		}
	}

}
