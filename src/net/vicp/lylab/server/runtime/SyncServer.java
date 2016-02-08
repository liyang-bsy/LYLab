package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * A server runtime based on Sync-Server.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends LoneWolf implements LifeCycle {
	private static final long serialVersionUID = 883892527805494627L;
	
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected ServerSocket serverSocket;
	protected LYTaskQueue taskQueue;
	protected Dispatcher<? super Confirm, ? super Confirm> dispatcher;
	protected Integer port = null;
	protected Protocol protocol;
	protected HeartBeat heartBeat;

	@Override
	public void initialize() {
		if(!closed.compareAndSet(true, false))
			return;
		try {
			if (port == null)
				throw new NullPointerException("Server port not defined");
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			throw new LYException("Server start failed", e);
		}
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!closed.compareAndSet(false, true))
			return;
		serverSocket.close();
		this.callStop();
	}

	@Override
	public void exec() {
		while (!isClosed()) {
			try {
				SyncSession session = new SyncSession(serverSocket, protocol, dispatcher, heartBeat);
				if(taskQueue.addTask(session) == null)
					await(CoreDef.WAITING_SHORT);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public boolean isClosed() {
		return closed.get();
	}

	public LYTaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LYTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public Dispatcher<? super Confirm, ? super Confirm> getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(Dispatcher<? super Confirm, ? super Confirm> dispatcher) {
		this.dispatcher = dispatcher;
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
