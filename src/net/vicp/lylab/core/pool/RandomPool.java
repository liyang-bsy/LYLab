package net.vicp.lylab.core.pool;

import java.util.HashSet;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;

/**
 * 随机读写数据池
 * 
 * @author liyang
 *
 */
public class RandomPool<T extends BaseObject> extends IndexedPool<T> {

	public RandomPool() {
		this(CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
	}

	public RandomPool(int maxSize) {
		super(new HashSet<Long>(), maxSize);
	}
	
}
