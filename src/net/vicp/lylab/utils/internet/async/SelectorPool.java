package net.vicp.lylab.utils.internet.async;

import java.nio.channels.Selector;
import java.util.Date;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.pool.TimeoutRecyclePool;
import net.vicp.lylab.utils.internet.HeartBeat;

/**
 * A selector pool maintenance specific selector.
 * You can access one by your needs, do not forget recycle it after using.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SelectorPool extends TimeoutRecyclePool<Selector> {
	protected String host;
	protected Integer port;
	protected HeartBeat heartBeat;
	protected Protocol protocol;

	public SelectorPool(String host, Integer port, Protocol protocol, HeartBeat heartBeat)
	{
		super();
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		this.heartBeat = heartBeat;
	}
	
	public SelectorPool(String host, Integer port, Protocol protocol, HeartBeat heartBeat, long timeout, int maxSize)
	{
		super(timeout, maxSize);
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		this.heartBeat = heartBeat;
	}
	
	protected Selector accessAndValidate()
	{
		Selector tmp = null;
		boolean isDead = true;
		int attemptCount = 0;
		do {
			tmp = getFromAvailableContainer();
			if(tmp == null)
			{
				if(size() > maxSize)
					return tmp;
				Long id = null;
				Selector passerby = null;
				try {
					passerby = Selector.open();
				} catch (Exception e) {
					throw new LYException(e);
				}
				while((id = add(passerby))==null) {
					attemptCount++;
					if(attemptCount > 10) throw new LYException("Attempt to add new instance to pool for 10 times, the container is full");
				}
				tmp = getFromAvailableContainer(id);
			}
		} while(isDead);
		return tmp;
	}

	@Override
	public Selector accessOne(boolean available) {
		safeCheck();
		synchronized (lock) {
			Selector tmp = null;
			if(available)
				tmp = accessAndValidate();
			else tmp = getFromBusyContainer();
			return tmp;
		}
	}
	
	@Override
	public void recycle() {
		synchronized (lock) {
			for (Long id : startTime.keySet()) {
				Date start = startTime.get(id);
				if (new Date().getTime() - start.getTime() > timeout) {
					Selector tmp = busyContainer.get(id);
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
