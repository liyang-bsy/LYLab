package net.vicp.lylab.utils.cache;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.core.model.CacheValue;
import net.vicp.lylab.utils.Algorithm;

/**
 * Local cache system.
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.2
 */
public final class LYCache extends NonCloneableBaseObject implements Initializable {
	private List<CacheContainer> bundles = null;
	private int containerSize = CoreDef.DEFAULT_LYCACHE_CONTAINER_SIZE;
	private long expireTime = CoreDef.DEFAULT_LYCACHE_EXPIRE_TIME;
	private long memoryLimitation = CoreDef.DEFAULT_LYCACHE_MEMORY_LIMITATION;
	private String hashAlgorithm = "MD5";
	public double threshold = CoreDef.DEFAULT_LYCACHE_THRESHOLD;

	@Override
	public synchronized void initialize() {
		ArrayList<CacheContainer> list = new ArrayList<CacheContainer>();
		for (int i = 0; i < containerSize; i++)
			list.add(new CacheContainer());
		this.bundles = list;
		
		setExpireTime(expireTime); // 30min = 60s*30min
		setMemoryControl(memoryLimitation, threshold); // 1GB
	}

	public void setMemoryControl(long memoryLimitation, double threshold) {
		if (threshold > 1.0D)
			threshold = 1.0D;
		setThreshold(threshold);
		setMemoryLimitation(memoryLimitation);
		List<CacheContainer> list = getBundles();
		for (CacheContainer item : list) {
			item.setMemoryLimitation(memoryLimitation / containerSize);
			item.setThreshold(threshold);
		}
		flush();
	}

	private CacheContainer getContainer(Integer seq) {
		if (seq < 0 || seq > getBundles().size())
			return null;
		return getBundles().get(seq);
	}

	private CacheContainer getContainer(String key) {
		Integer seq = keyRule(key);
		return getContainer(seq);
	}

	private int keyRule(String key) {
		return Math.abs(Algorithm.hash(key, hashAlgorithm)) % containerSize;
	}

	public List<CacheContainer> getBundles() {
		return bundles;
	}

	public long getEntrySize() {
		long size = 0;
		for (CacheContainer cc : getBundles())
			size += cc.size();
		return size;
	}

	public long getMemorySize() {
		long size = 0;
		for (CacheContainer cc : getBundles())
			size += cc.getMemoryUsage();
		return size;
	}

	// function start
	public int setCacheValue(String key, CacheValue cv) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return 1;
		return cc.setCacheValue(key, cv);
	}
	
	public int set(String key, byte[] value) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return 1;
		return cc.set(key, value, expireTime);
	}

	public byte[] get(String key) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.get(key);
	}

	public byte[] get(String key, boolean renew) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.get(key, renew);
	}

	public byte[] delete(String key) {
		CacheContainer cc = getContainer(key);
		if (cc == null)
			return null;
		return cc.delete(key);
	}

	public boolean flush() {
		try {
			for (CacheContainer cc : getBundles())
				cc.flush();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void clear() {
		for (CacheContainer cc : getBundles())
			cc.clear();
	}

	// getter & setter
	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

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

	public int getContainerSize() {
		return containerSize;
	}

	public void setContainerSize(int containerSize) {
		this.containerSize = containerSize;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

}
