package net.vicp.lylab.core.pool;

import java.util.HashSet;

import net.vicp.lylab.core.CoreDef;

/**
 * 
 * @author liyang
 *
 * 顺序化数据池
 *
 */
public class RandomPool<T> extends IndexedPool<T> {

	public RandomPool() {
		this(CoreDef.DEFAULT_POOL_MAX_SIZE);
	}

	public RandomPool(int maxSize) {
		super(new HashSet<Long>(), maxSize);
	}
	
}
