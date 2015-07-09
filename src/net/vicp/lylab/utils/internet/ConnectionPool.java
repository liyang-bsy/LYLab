package net.vicp.lylab.utils.internet;

import java.util.Date;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.recycle.Refiller;
import net.vicp.lylab.core.pool.TimeoutRecyclePool;

/**
 * A connection pool maintenance specific connections.
 * You can access one or many by your needs, do not forget recycle socket after close.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ConnectionPool<T> extends TimeoutRecyclePool<T> {
	protected String host;
	protected Integer port;
	protected Refiller<T> user;

	public ConnectionPool(Refiller<T> user, String host, Integer port)
	{
		super();
		this.host = host;
		this.port = port;
		this.user = user;
		recover();
	}
	
	public ConnectionPool(Refiller<T> user, String host, Integer port, long timeout, int maxSize)
	{
		super(timeout, maxSize);
		this.host = host;
		this.port = port;
		this.user = user;
		recover();
	}
	
	@Override
	public void recycle() {
		for (Long id : startTime.keySet()) {
			Date start = startTime.get(id);
			if (new Date().getTime() - start.getTime() > timeout) {
				try {
					T tmp = busyContainer.get(id);
					if(tmp instanceof AutoCloseable)
						((AutoCloseable) tmp).close();
					busyContainer.remove(id);
					keyContainer.remove(id);
					startTime.remove(id);
				} catch (Exception e) {
					throw new LYException("Recycle failed", e);
				}
			}
		}
		recover();
		safeCheck();
	}

	/**
	 * Refill destroyed sockets
	 */
	public void recover()
	{
		while (maxSize > size()) {
			int count = maxSize - size();
			for (int i = 0; i < count; i++) {
				add((T) user.refill());
			}
		}
	}

}
