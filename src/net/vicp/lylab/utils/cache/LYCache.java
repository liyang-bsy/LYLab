package net.vicp.lylab.utils.cache;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.utils.MD5;
import net.vicp.lylab.utils.tq.Task;

public final class LYCache {
	private List<CacheContainer> bundles = null;
	private static Long expireTime;
	private static Long memoryLimit;
	public static Double threshold;

	private static volatile int flushCnt = 0;
	private static LYCache instance;
	
	public LYCache()
	{
		LYCache.setMemoryLimit(expireTime != null ? expireTime : 4*1024*1024*1024L);		// 4GB
		LYCache.setExpireTime(memoryLimit != null ? memoryLimit : 1000*60*30L);				// 30min = 60s*30min
		getBundles();
	}
	
	private static CacheContainer getContainer(Integer seq) {
		if(seq < 0 || seq > getBundles().size())
			return null;
		return getBundles().get(seq);
	}
	
	private static CacheContainer getContainer(String key) {
		Integer seq = keyRule(key);
		return getContainer(seq);
	}
	
	public static List<CacheContainer> getBundles()
	{
		if(getInstance().bundles == null)
		{
			getInstance().bundles = new ArrayList<CacheContainer>();
			for(int i=0;i<16;i++)
				getInstance().bundles.add(new CacheContainer());
		}
		return getInstance().bundles;
	}

	public static LYCache getInstance() {
		if(instance == null)
			instance = new LYCache();
		return instance;
	}

	public static long getEntrySize() {
		long size = 0;
		for(CacheContainer cc : getBundles())
		{
			size += cc.getContainer().size();
		}
		return size;
	}
	
	public static long getMemorySize() {
		long size = 0;
		for(CacheContainer cc : getBundles())
		{
			size += cc.getMemoryUsage();
		}
		return size;
	}
	
	public static int keyRule(String key)
	{
		String md5 = MD5.md5_32(key).toLowerCase();
		char c = md5.charAt(0);
		int ret = -1;
		if(c >= '0' && c <= '9')
			ret = c - '0';
		else
			ret = c - 'a' + 10;
		return ret;
	}

	// function start
	public static int set(String key, byte[] value)
	{
		CacheContainer cc = getContainer(key);
		if(cc == null) return 1;
		return cc.set(key, value, expireTime);
	}
	
	public static byte[] get(String key)
	{
		CacheContainer cc = getContainer(key);
		if(cc == null) return null;
		return cc.get(key);
	}
	
	public static byte[] get(String key, boolean renew)
	{
		CacheContainer cc = getContainer(key);
		if(cc == null) return null;
		return cc.get(key, renew);
	}

	public static byte[] delete(String key)
	{
		CacheContainer cc = getContainer(key);
		if(cc == null) return null;
		return cc.delete(key);
	}
	
	public static boolean flush()
	{
		try {
			Task last = null;
			for(CacheContainer cc : getBundles())
			{
				flushCnt++;
				last = new Task() {
					CacheContainer c;
					private static final long serialVersionUID = 1L;
					@Override
					public void exec() {
						c.flush();
					}
					public Task setC(CacheContainer cc) {
						this.c = cc;
						return this;
					}
					public void aftermath() {
						flushCnt--;
					};
				}.setC(cc);
				last.begin();
			}
			while(flushCnt > 0)
				Thread.sleep(50);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String version()
	{
		return CacheContainer.version();
	}

	// getter & setter

	public static Long getExpireTime() {
		return expireTime;
	}

	public static void setExpireTime(Long expireTime) {
		LYCache.expireTime = expireTime;
	}

	public static Long getMemoryLimit() {
		return memoryLimit;
	}

	public static void setMemoryLimit(Long memoryLimit) {
		LYCache.memoryLimit = memoryLimit;
		List<CacheContainer> list = getBundles();
		for(CacheContainer item : list)
		{
			item.setMemoryLimitation(memoryLimit/16);
			item.setThreshold(threshold);
		}
	}

}
