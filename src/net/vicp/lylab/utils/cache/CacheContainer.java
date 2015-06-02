package net.vicp.lylab.utils.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.model.CacheValue;
import net.vicp.lylab.utils.tq.Task;

public final class CacheContainer {

	private static final String version = "0.0.1";
	private Map<String, CacheValue> container = new ConcurrentHashMap<String, CacheValue>();
	public static long MemoryLimitation = 1024*1024*1024L;
	public static double threshold = 0.8;
	private volatile long memoryUsage = 0L;

	public Map<String, CacheValue> getContainer() {
		if(container == null) container = new ConcurrentHashMap<String, CacheValue>();
		return container;
	}

	public long getMemoryUsage() {
		return memoryUsage;
	}
	
	// function start
	public int set(String key, byte[] value, Long expireTime)
	{
		if(MemoryLimitation < memoryUsage + value.length)
			return 2;
		getContainer().put(key, new CacheValue(value, expireTime));
		memoryUsage += value.length;
		if(memoryUsage > MemoryLimitation*threshold)
		{
			new Task() {
				CacheContainer c;
				private static final long serialVersionUID = 6661384694891274270L;
				@Override
				public void exec() {
					double dec = 1.0;
					do
					{
						System.out.println(dec);
						c.flush(dec);
						dec/=2;
					} while(c.getMemoryUsage() > CacheContainer.getMemoryLimitation()*CacheContainer.threshold);
				}
				public Task setC(CacheContainer cc) {
					this.c = cc;
					return this;
				}
			}.setC(this).begin();
		}
		return 0;
	}
	
	public byte[] get(String key)
	{
		CacheValue cv = getContainer().get(key);
		if(cv == null) return null;
		if(cv.getStartTime() + cv.getValidateTime() < System.currentTimeMillis())
		{
			remove(key, cv);
			return null;
		}
		cv.renewStartTime();
		return cv.getValue();
	}

	public byte[] delete(String key)
	{
		CacheValue cv = getContainer().get(key);
		if(cv == null) return null;
		remove(key, cv);
		return cv.getValue();
	}
	
	public void remove(String key, CacheValue cv)
	{
		getContainer().remove(key);
		memoryUsage -= cv.getValue().length;
	}

	public boolean flush()
	{
		return flush(1.0);
	}
	
	public boolean flush(double dec)
	{
		boolean result = false;
		Long newMemoryUsage = 0L;
		memoryUsage = Long.MAX_VALUE;
		Iterator<String> it = getContainer().keySet().iterator();  
		while (it.hasNext())
        {
			String key = it.next();
			CacheValue cv = getContainer().get(key);
			if(cv == null) continue;
			if(cv.getStartTime() + dec*cv.getValidateTime() < System.currentTimeMillis())
				getContainer().remove(key);
			else newMemoryUsage += cv.size();
		}
		memoryUsage = newMemoryUsage;
		return result;
	}

	public static String version()
	{
		return version;
	}

	// getter & setter

	public static long getMemoryLimitation() {
		return MemoryLimitation;
	}

	public static void setMemoryLimitation(long memoryLimitation) {
		MemoryLimitation = memoryLimitation;
	}

}
