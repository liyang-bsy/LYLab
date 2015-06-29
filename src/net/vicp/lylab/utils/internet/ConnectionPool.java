package net.vicp.lylab.utils.internet;

import java.util.Date;
import java.util.Map;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.pool.TimeoutRecyclePool;
import net.vicp.lylab.utils.atomic.AtomicWeakReference;

/**
 * 
 * @author liyang
 *
 * 抽象的回收池
 *
 */
public class ConnectionPool extends TimeoutRecyclePool<AutoCloseable> {
	protected Map<Long, Date> startTime;
	protected Long timeout;
	
	protected String host;
	protected Integer port;

	public ConnectionPool(String host, Integer port)
	{
		super();
		this.host = host;
		this.port = port;
	}
	
	public ConnectionPool(String host, Integer port, long timeout, int maxSize)
	{
		super(timeout, maxSize);
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void recycle() {
		for (Long id : startTime.keySet()) {
			Date start = startTime.get(id);
			if (new Date().getTime() - start.getTime() > timeout) {
				try {
					AutoCloseable tmp = busyContainer.get(id);
					if (tmp != null) {
						tmp.close();
						busyContainer.remove(id);
						keyContainer.remove(id);
						startTime.remove(id);
					}
				} catch (Exception e) {
					throw new LYException("Recycle failed", e);
				}
			}
		}
		recover();
		safeCheck();
	}

	public void recover()
	{
		while (maxSize > size()) {
			int count = maxSize - size();
			for (int i = 0; i < count; i++) {
				add(new AtomicWeakReference<LYSocket>().get(LYSocket.class, host, port));
			}
		}
	}

	@Override
	public boolean isRecyclable() {
		safeCheck();
		return startTime.size() != 0;
	}

}
