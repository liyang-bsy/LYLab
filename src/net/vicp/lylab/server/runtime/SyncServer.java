package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.server.aop.DefaultAop;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * Actually, this class is not as useful as I thought
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends Task implements LifeCycle {
	private static final long serialVersionUID = 883892527805494627L;
	protected AtomicBoolean isClosed = new AtomicBoolean(true);
	protected ServerSocket serverSocket;
	protected LYTaskQueue lyTaskQueue = null;
	protected Aop aop;
	protected Integer port = null;

	public SyncServer() {
		this(new DefaultAop());
	}

	public SyncServer(Aop aop) {
		this.aop = aop;
	}
	
	@Override
	public void initialize() {
		if(!isClosed.compareAndSet(true, false))
			return;
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!isClosed.compareAndSet(false, true))
			return;
		this.callStop();
		serverSocket.close();
	}

	@Override
	public void exec() {
		if (this.lyTaskQueue == null) {
			lyTaskQueue = new LYTaskQueue();
			try {
				lyTaskQueue.setMaxQueue(CoreDef.config.getConfig("SyncServer")
						.getInteger("maxQueue"));
			} catch (Exception e) {
			}
			try {
				lyTaskQueue.setMaxThread(CoreDef.config.getConfig("SyncServer")
						.getInteger("maxThread"));
			} catch (Exception e) {
			}
		}
		try {
			port = CoreDef.config.getConfig("SyncServer").getInteger("port");
		} catch (Exception e) {
		}
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			throw new LYException("Server start failed", e);
		}
		while (!isClosed.get()) {
			try {
				lyTaskQueue.addTask(new ToClientLongSocket(serverSocket, new SimpleHeartBeat()).setAopLogic(aop));
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		if(this.lyTaskQueue == null)
			this.lyTaskQueue = lyTaskQueue;
	}

	public void setAop(Aop aop) {
		this.aop = aop;
	}

	public boolean isClosed() {
		return isClosed.get();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
