package net.vicp.lylab.server.runtime;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.AsyncSession;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * A server runtime based on Async-Server, AsyncSocket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2016.01.22
 * @version 1.0.0
 */
public class AsyncServer extends LoneWolf implements LifeCycle {
	private static final long serialVersionUID = -6058980641184489541L;
	
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected Session session;
	protected LYTaskQueue taskQueue;
	protected Dispatcher<? super Confirm, ? super Confirm> dispatcher;
	protected Integer port = null;
	protected Protocol protocol;
	protected HeartBeat heartBeat;

	@Override
	public void initialize() {
		if(!closed.compareAndSet(true, false))
			return;
		this.begin("Async Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!closed.compareAndSet(false, true))
			return;
		session.close();
		this.callStop();
	}

	@Override
	public void exec() {
		session = new AsyncSession(port, protocol, dispatcher, heartBeat, taskQueue);
		session.initialize();
	}

	public void setLongServer(boolean longServer) {
//		this.longServer = longServer;
		if(longServer == false)
			throw new LYException("AsyncServer can only be a long server");
	}

	public boolean isClosed() {
		return closed.get();
	}

	public Dispatcher<? super Confirm, ? super Confirm> getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(Dispatcher<? super Confirm, ? super Confirm> dispatcher) {
		this.dispatcher = dispatcher;
	}

	public LYTaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LYTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isLongServer() {
		return heartBeat != null;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public HeartBeat getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(HeartBeat heartBeat) {
		this.heartBeat = heartBeat;
	}

}
