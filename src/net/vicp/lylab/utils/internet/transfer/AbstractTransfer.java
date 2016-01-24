package net.vicp.lylab.utils.internet.transfer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transfer;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.controller.TimeoutController;
import net.vicp.lylab.utils.internet.Session;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public abstract class AbstractTransfer extends LoneWolf implements Transfer {
	private static final long serialVersionUID = 1706498472211433734L;
	
	//					client		validate
	protected Map<InetSocketAddress, Boolean> addr2validate = new HashMap<>();
	//					client		startTime
	protected Map<InetSocketAddress, Long> addr2timeout = new HashMap<>();
	//					client				byte buffer
	protected Map<InetSocketAddress, Pair<byte[], Integer>> addr2byte = new HashMap<>();
	//										client			Data
	protected SequenceTemporaryPool<Pair<InetSocketAddress, byte[]>> requestPool = new SequenceTemporaryPool<>();
	//
	protected long timeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected Session session;
	protected Protocol protocol;
	protected LYTaskQueue taskQueue;

	@Override
	public void initialize() {
		if(protocol == null)
			throw new LYException("No protocol is assigned");
		if(taskQueue == null)
			throw new LYException("No taskQueue is assigned");
		if(session == null)
			throw new LYException("No session is assigned");
		if (!closed.compareAndSet(true, false))
			return;
		TimeoutController.addToWatch(this);
		this.begin("transfer");
	}

	@Override
	public void putRequest(Socket client, byte[] buffer, int bufferLen) {
		InetSocketAddress clientAddr = (InetSocketAddress) client.getRemoteSocketAddress();
		if (!addr2byte.containsKey(clientAddr))
			addr2byte.put(clientAddr, new Pair<>(buffer, bufferLen));
		else {
			Pair<byte[], Integer> container = addr2byte.get(clientAddr);
			synchronized (lock) {
				container.setLeft(Utils.bytecat(container.getLeft(), container.getRight(),
						buffer, 0, bufferLen));
				container.setRight(container.getRight() + bufferLen);
			}
		}
		addr2validate.put(clientAddr, true);
		addr2timeout.put(clientAddr, System.currentTimeMillis());
		//requestPool.add(new Pair<>(socketChannel, request));
		signalAll();
	}

	public Pair<byte[], Integer> getRequest(Socket client) {
		synchronized (lock) {
			InetSocketAddress clientAddr = (InetSocketAddress) client.getRemoteSocketAddress();
			if (!addr2byte.containsKey(clientAddr)) {
				addr2timeout.remove(clientAddr);
				addr2validate.remove(clientAddr);
				return addr2byte.remove(clientAddr);
			}
			throw new LYException("No match request was found");
		}
	}

	@Override
	public boolean isRecyclable() {
		return !addr2timeout.isEmpty();
	}

	@Override
	public void recycle() {
		recycle(1.0);
	}
	
	public void recycle(double rate) {
		synchronized (lock) {
			Iterator<Entry<InetSocketAddress, Long>> addrIterator = addr2timeout.entrySet().iterator();
			while (addrIterator.hasNext()) {
				Entry<InetSocketAddress, Long> entry = addrIterator.next();
				if (System.currentTimeMillis() - entry.getValue() > timeout * rate) {
					addr2validate.remove(entry.getKey());
					addr2byte.remove(entry.getKey());
					addrIterator.remove();
				}
			}
		}
	}

	// getters & setters

	public long getTimeout() {
		return timeout;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public LYTaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LYTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

}
