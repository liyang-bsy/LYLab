package net.vicp.lylab.core.datastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDefine;

/**
 * 
 * @author liyang
 *
 *         STL: “池”，自动管理的并发池的抽象类
 *
 */
public abstract class AbstractPool<R> implements Pool<R> {

	protected Map<Long, R> container;
	protected Map<Long, Boolean> keyList;
	protected Long minId = Long.MAX_VALUE, maxId = Long.MIN_VALUE;
	public static final Integer DEFAULT_maxSize = 16;
	public Integer maxSize;

	public AbstractPool() {
		this(new ConcurrentHashMap<Long, R>());
	}
	
	public AbstractPool(Map<Long, R> c) {
		this(c, DEFAULT_maxSize);
	}
	
	public AbstractPool(Map<Long, R> c, Integer maxSize) {
		this.maxSize = maxSize;
		container = c;
		keyList = new HashMap<Long, Boolean>();
	}

	public void clear() throws IOException {
		keyList.clear();
		container.clear();
	}

	@Override
	public synchronized void add(R t) {
		while (true) {
			Integer size = container.size();
			if (size >= maxSize) {
				try {
					container.wait(CoreDefine.waitingThreshold);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (size <= maxSize && size >= 0) {
				if(maxId == Long.MAX_VALUE) maxId = 0L;
				((BaseObject) t).setObjId(maxId);
				container.put(maxId, t);
				break;
			}
		}
	}
	
}
