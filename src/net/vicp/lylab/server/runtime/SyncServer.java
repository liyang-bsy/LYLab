package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.BaseSocket;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.internet.ToClientSocket;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A server runtime based on Sync-Server, TaskSocket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends Task implements LifeCycle {
	private static final long serialVersionUID = 883892527805494627L;
	
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected ServerSocket serverSocket;
	protected LYTaskQueue lyTaskQueue = null;
	protected Aop aop;
	protected Integer port = null;
	protected boolean longServer = false;

	@Override
	public void initialize() {
		if(!closed.compareAndSet(true, false))
			return;
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
		try {
			if(port == null) throw new NullPointerException("Server port not defined");
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			throw new LYException("Server start failed", e);
		}
		while (!isClosed()) {
			try {
				BaseSocket bs = null;
				if(isLongServer())
					bs = new ToClientLongSocket(serverSocket, new SimpleHeartBeat()).setAopLogic(aop);
				else
					bs = new ToClientSocket(serverSocket).setAopLogic(aop);
				if(lyTaskQueue.addTask(bs) == null)
					await(CoreDef.WAITING_SHORT);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		this.lyTaskQueue = lyTaskQueue;
	}

	public void setAop(Aop aop) {
		this.aop = aop;
	}

	public boolean isClosed() {
		return closed.get();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLongServer() {
		return longServer;
	}

	public void setLongServer(boolean longServer) {
		this.longServer = longServer;
	}

}
