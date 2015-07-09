package net.vicp.lylab.utils.cache;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.MD5;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;

/**
 * Local cache system.
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.2
 */
public final class LYCache extends NonCloneableBaseObject implements LifeCycle {
	private List<CacheContainer> bundles = null;
	private long expireTime = CoreDef.DEFAULT_LYCACHE_EXPIRE_TIME;
	private long memoryLimitation = CoreDef.DEFAULT_LYCACHE_MEMORY_LIMITATION;
	public double threshold = CoreDef.DEFAULT_LYCACHE_THRESHOLD;

	private static AutoInitialize<LYCache> instance = new AtomicStrongReference<LYCache>();

	@Override
	public synchronized void initialize() {
		ArrayList<CacheContainer> list = new ArrayList<CacheContainer>();
		for (int i = 0; i < 16; i++)
			list.add(new CacheContainer());
		getInstance().bundles = list;
		setExpireTime(expireTime); // 30min = 60s*30min
		LYCache.setMemoryControl(memoryLimitation, threshold); // 1GB
	}

	@Override
	public synchronized void terminate() {
	}

	public static void setMemoryControl(long memoryLimitation, double threshold) {
		if (threshold > 1.0D)
			threshold = 1.0D;
		setThreshold(threshold);
		setMemoryLimitation(memoryLimitation);
		List<CacheContainer> list = getBundles();
		for (CacheContainer item : list) {
			item.setMemoryLimitation(memoryLimitation / 16);
			item.setThreshold(threshold);
		}
		LYCache.flush();
	}

	private static CacheContainer getContainer(Integer seq) {
		if (seq < 0 || seq > getBundles().size())
			return null;
		return getBundles().get(seq);
	}

	private static CacheContainer getContainer(String key) {
		Integer seq = keyRule(key);
		return getContainer(seq);
	}

	public static List<CacheContainer> getBundles() {
		return getInstance().bundles;
	}

	public static LYCache getInstance() {
		return instance.get(LYCache.class);
	}

	public static long getEntrySize() {
		long size = 0;
		for (CacheContainer cc : getBundles())
			size += cc.size();
		return size;
	}

	public static long getMemorySize() {
		long size = 0;
		for (CacheContainer cc : getBundles())
			size += cc.getMemoryUsage();
		return size;
	}

	public static int keyRule(String key) {
		String md5 = MD5.md5_32(key).toLowerCase();
		char c = md5.charAt(0);
		int ret = -1;
		if (c >= '0' && c <= '9')
			ret = c - '0';
		else
			ret = c - 'a' + 10;
		return ret;
	}

	// function start
	public static int set(String key, byte[] value) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return 1;
		return cc.set(key, value, getInstance().expireTime);
	}

	public static byte[] get(String key) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.get(key);
	}

	public static byte[] get(String key, boolean renew) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.get(key, renew);
	}

	public static byte[] delete(String key) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.delete(key);
	}

	public static boolean flush() {
		try {
			for (CacheContainer cc : getBundles())
				cc.flush();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void clear() {
		for (CacheContainer cc : getBundles())
			cc.clear();
	}

	public static String version() {
		return CacheContainer.version();
	}

	// getter & setter
	public static long getExpireTime() {
		return getInstance().expireTime;
	}

	public static void setExpireTime(long expireTime) {
		getInstance().expireTime = expireTime;
	}

	public static long getMemoryLimitation() {
		return getInstance().memoryLimitation;
	}

	public static void setMemoryLimitation(long memoryLimitation) {
		getInstance().memoryLimitation = memoryLimitation;
	}

	public static double getThreshold() {
		return getInstance().threshold;
	}

	public static void setThreshold(double threshold) {
		getInstance().threshold = threshold;
	}

}
