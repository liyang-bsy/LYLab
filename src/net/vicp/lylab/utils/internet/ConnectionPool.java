package net.vicp.lylab.utils.internet;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Initialize;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.Protocol;
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
	protected Class<T> prototypeClass;
	protected HeartBeat heartBeat;
	protected Protocol protocol;

	public ConnectionPool(Class<T> prototypeClass, String host, Integer port, Protocol protocol, HeartBeat heartBeat)
	{
		super();
		this.host = host;
		this.port = port;
		this.prototypeClass = prototypeClass;
		this.protocol = protocol;
		this.heartBeat = heartBeat;
	}
	
	public ConnectionPool(Class<T> prototypeClass, String host, Integer port, Protocol protocol, HeartBeat heartBeat, long timeout, int maxSize)
	{
		super(timeout, maxSize);
		this.host = host;
		this.port = port;
		this.prototypeClass = prototypeClass;
		this.protocol = protocol;
		this.heartBeat = heartBeat;
	}
	
	protected T accessAndValidate()
	{
		T tmp = null;
		boolean isDead = true;
		int attemptCount = 0;
		do {
			tmp = getFromAvailableContainer();
			if(tmp == null)
			{
				if(size() > maxSize)
					return tmp;
				
				attemptCount++;
				if(attemptCount > 5) throw new LYException("Attempt to create new instance for 5 times");
				
				Long id = null;
				try {
					Constructor<T> con = prototypeClass.getConstructor(host.getClass(), port.getClass(), Protocol.class, HeartBeat.class);
					T passerby = con.newInstance(host, port, protocol, heartBeat);
					if(passerby == null)
						throw new LYException("Create prototype instance failed");
					if(passerby instanceof Initialize)
						((Initialize) passerby).initialize();
					id = add(passerby);
				} catch (Exception e) {
					throw new LYException("Prototype class must have a constructor with param"
							+ "(String host, Integer port, Protocol protocol, HeartBeat heartBeat", e);
				}
				tmp = getFromAvailableContainer(id);
			}
			if(tmp instanceof KeepAlive && ((KeepAlive) tmp).isAlive())
				isDead = false;
			else {
				searchAndRemove(tmp);
			}
		} while(isDead);
		return tmp;
	}

	@Override
	public T accessOne(boolean available) {
		safeCheck();
		synchronized (lock) {
			T tmp = null;
			try {
				if(available)
					tmp = accessAndValidate();
				else tmp = getFromBusyContainer();
			} catch (Exception e) { }
			return tmp;
		}
	}
	
	public List<T> accessMany(int amount, boolean available) {
		throw new LYException("Access many is not support for a connection pool");
	}

	@Override
	public void recycle() {
		synchronized (lock) {
			for (Long id : startTime.keySet()) {
				Date start = startTime.get(id);
				if (new Date().getTime() - start.getTime() > timeout) {
					T tmp = busyContainer.get(id);
					if (tmp != null) {
						try {
							if (tmp instanceof AutoCloseable) {
								((AutoCloseable) tmp).close();
							}
						} catch (Exception e) {
							throw new LYException("Recycle failed", e);
						}
						busyContainer.remove(id);
						keyContainer.remove(id);
						startTime.remove(id);
					}
				}
			}
			for (Long id : availableKeySet()) {
				T tmp = getFromAvailableContainer(id);
				if(tmp instanceof KeepAlive)
				{
					if (((KeepAlive) tmp).isDying()) {
						try {
							((KeepAlive) tmp).keepAlive();
						} catch (Throwable e) {
							throw new LYException("Keep alive failed", e);
						}
					}
				}
			}
		}
//		recover();
		safeCheck();
	}

//	/**
//	 * Refill destroyed sockets
//	 */
//	public void recover()
//	{
//		while (maxSize > size()) {
//			int count = maxSize - size();
//			for (int i = 0; i < count; i++) {
//				add((T) user.refill());
//			}
//		}
//	}

}
