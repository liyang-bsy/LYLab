package net.vicp.lylab.utils.cache;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.utils.MD5;

public final class LYCache {
	private List<CacheContainer> bundles = null;
	private static long expireTime;
	private static long memoryLimitation;
	public static double threshold;

	private static LYCache instance;
	
	public LYCache()
	{
		if(instance == null)
			instance = this;
		else return;
		LYCache.setExpireTime(1000*60*30L);							// 30min = 60s*30min
		LYCache.setMemoryControl(1024*1024*1024L, 0.8D);				// 1GB
		getBundles();
	}
	
	public LYCache(long expire, long memLimit, double threshold)
	{
		if(instance == null)
			instance = this;
		else return;
		LYCache.setExpireTime(expire);
		LYCache.setMemoryControl(memLimit, threshold);
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
		if(instance == null) new LYCache();
		return instance;
	}

	public static long getEntrySize() {
		long size = 0;
		for(CacheContainer cc : getBundles())
		{
			size += cc.size();
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
			for(CacheContainer cc : getBundles())
				cc.flush();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static void clear()
	{
		for(CacheContainer cc : getBundles())
			cc.clear();
	}

	public static String version()
	{
		return CacheContainer.version();
	}

	// getter & setter

	public static long getExpireTime() {
		return expireTime;
	}

	public static void setExpireTime(long expireTime) {
		LYCache.expireTime = expireTime;
	}

	public static long getMemoryLimitation() {
		return memoryLimitation;
	}

	public static double getThreshold() {
		return threshold;
	}

	public static void setMemoryControl(long memoryLimitation, double threshold) {
		if(threshold > 1.0D) threshold = 1.0D;
		LYCache.threshold = threshold;
		LYCache.memoryLimitation = memoryLimitation;
		List<CacheContainer> list = getBundles();
		for(CacheContainer item : list)
		{
			item.setMemoryLimitation(memoryLimitation/16);
			item.setThreshold(threshold);
		}
		LYCache.flush();
	}

}