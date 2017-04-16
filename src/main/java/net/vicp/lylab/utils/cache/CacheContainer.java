package net.vicp.lylab.utils.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.model.CacheValue;
import net.vicp.lylab.utils.atomic.AtomicLong;
import net.vicp.lylab.utils.tq.Task;

/**
 * Cache container.
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.2
 */
public final class CacheContainer extends NonCloneableBaseObject {

	private Map<String, CacheValue> container = new ConcurrentHashMap<String, CacheValue>();
	public long memoryLimitation;
	public double threshold;
	private AtomicLong memoryUsage = new AtomicLong(0L);

	public int size() {
		return container.size();
	}

	// function start
	public final int set(String key, byte[] value) {
		return setCacheValue(key, new CacheValue(value, 0));
	}
	
	public final int set(String key, byte[] value, int expireTime) {
		return setCacheValue(key, new CacheValue(value, expireTime));
	}
	
	public final int setCacheValue(String key, CacheValue cv) {
		if (memoryLimitation - getMemoryUsage() < cv.getValue().length)
			return 2;
		memoryUsage.getAndAdd(cv.getValue().length + key.getBytes().length + 8);
		container.put(key, cv);
		if (getMemoryUsage() > memoryLimitation * threshold) {
			new Task() {
				private static final long serialVersionUID = 6661384694891274270L;

				CacheContainer c;

				@Override
				public void exec() {
					double dec = 1.0;
					do {
						c.flush(dec);
						dec /= 2;
					} while (c.getMemoryUsage() > c.getMemoryLimitation() * c.threshold / 2);
				}

				public Task setCacheContainer(CacheContainer cc) {
					this.c = cc;
					return this;
				}
			}.setCacheContainer(this).begin();
		}
		return 0;
	}
	
	public CacheValue getCacheValue(String key) {
		return container.get(key);
	}

	public byte[] get(String key, boolean renew) {
		CacheValue cv = container.get(key);
		if (cv == null)
			return null;
		if (cv.getValidateTime() < System.currentTimeMillis() - cv.getStartTime()) {
			remove(key);
			return null;
		}
		if (renew)
			cv.setStartTime(System.currentTimeMillis());
		return cv.getValue();
	}

	public byte[] delete(String key) {
		CacheValue cv = container.get(key);
		if (cv == null)
			return null;
		remove(key);
		return cv.getValue();
	}

	private void remove(String key) {
		CacheValue cv = container.remove(key);
		if (cv != null)
			memoryUsage.getAndAdd(-1 * (cv.getValue().length + key.getBytes().length + 8));
	}

	public void clear() {
		memoryUsage.set(0L);
		container.clear();
	}

	public boolean flush() {
		return flush(1.0D);
	}

	public boolean flush(double dec) {
		boolean result = false;
		Long newMemoryUsage = 0L;
		memoryUsage.set(Long.MAX_VALUE);
		Iterator<String> it = container.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			CacheValue cv = container.get(key);
			if (cv == null)
				continue;
			if (dec * cv.getValidateTime() < System.currentTimeMillis() - cv.getStartTime())
				container.remove(key);
			else
				newMemoryUsage += cv.getValue().length + key.getBytes().length + 8;
		}
		memoryUsage.set(newMemoryUsage);
		return result;
	}

	public boolean containsKey(Object key) {
		return container.containsKey(key);
	}

	public Set<String> keySet() {
		return container.keySet();
	}

	public long getMemoryUsage() {
		return memoryUsage.get();
	}
	
	// getter & setter
	public long getMemoryLimitation() {
		return memoryLimitation;
	}

	public void setMemoryLimitation(long memoryLimitation) {
		this.memoryLimitation = memoryLimitation;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
