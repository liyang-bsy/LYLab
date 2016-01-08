package net.vicp.lylab.core.pool;

import java.util.LinkedList;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;

/**
 * 顺序化数据池
 * 
 * @author liyang
 *
 */
public class SequencePool<T extends BaseObject> extends IndexedPool<T> {

	public SequencePool() {
		this(CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}

	public SequencePool(int maxSize) {
		super(new LinkedList<Long>(), maxSize);
	}
	
	public Long add(int index, T t) {
		synchronized (lock) {
			safeCheck();
			Long id = addToContainer(t);
			if(id != null && !keyContainer.contains(id))
				((LinkedList<Long>) keyContainer).add(index, id);
			return id;
		}
	}

}
