package net.vicp.lylab.utils.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.model.CacheValue;
import net.vicp.lylab.utils.tq.Task;

/**
 * Cache container.
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.2
 */
public final class CacheContainer extends NonCloneableBaseObject {

	private static final String version = "1.0.2";
	private Map<String, CacheValue> container = new ConcurrentHashMap<String, CacheValue>();
	public long memoryLimitation;
	public double threshold;
	private volatile long memoryUsage = 0L;

	private Map<String, CacheValue> getContainer() {
		if (container == null)
			synchronized (lock) {
				if (container == null)
					container = new ConcurrentHashMap<String, CacheValue>();
			}
		return container;
	}

	public long getMemoryUsage() {
		return memoryUsage;
	}

	public int size() {
		return getContainer().size();
	}

	// function start
	public int set(String key, byte[] value, Long expireTime) {
		if (memoryLimitation - memoryUsage < value.length)
			return 2;
		getContainer().put(key, new CacheValue(value, expireTime));
		memoryUsage += value.length;
		if (memoryUsage > memoryLimitation * threshold) {
			new Task() {
				private static final long serialVersionUID = 6661384694891274270L;
				
				CacheContainer c;
				@Override
				public void exec() {
					double dec = 1.0;
					do {
						c.flush(dec);
						dec /= 2;
					} while (c.getMemoryUsage() > c.getMemoryLimitation()
							* c.threshold / 2);
				}
				public Task setC(CacheContainer cc) {
					this.c = cc;
					return this;
				}
			}.setC(this).begin();
		}
		return 0;
	}
	
	public CacheValue getCacheValue(String key) {
		return getContainer().get(key);
	}

	public byte[] get(String key) {
		return get(key, false);
	}

	public byte[] get(String key, boolean renew) {
		CacheValue cv = getContainer().get(key);
		if (cv == null)
			return null;
		if (cv.getValidateTime() < System.currentTimeMillis() - cv.getStartTime()) {
			remove(key, cv);
			return null;
		}
		if (renew)
			cv.renewStartTime();
		return cv.getValue();
	}

	public byte[] delete(String key) {
		CacheValue cv = getContainer().get(key);
		if (cv == null)
			return null;
		remove(key, cv);
		return cv.getValue();
	}

	public void remove(String key, CacheValue cv) {
		getContainer().remove(key);
		memoryUsage -= cv.getValue().length;
	}

	public void clear() {
		memoryUsage = 0L;
		getContainer().clear();
	}

	public boolean flush() {
		return flush(1.0D);
	}

	public boolean flush(double dec) {
		boolean result = false;
		Long newMemoryUsage = 0L;
		memoryUsage = Long.MAX_VALUE;
		Iterator<String> it = getContainer().keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			CacheValue cv = getContainer().get(key);
			if (cv == null)
				continue;
			if (dec * cv.getValidateTime() < System.currentTimeMillis() - cv.getStartTime())
				getContainer().remove(key);
			else
				newMemoryUsage += cv.size();
		}
		memoryUsage = newMemoryUsage;
		return result;
	}

	public static String version() {
		return version;
	}

	public boolean containsKey(Object key) {
		return container.containsKey(key);
	}

	public Set<String> keySet() {
		return container.keySet();
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

	public static String getVersion() {
		return version;
	}

}
