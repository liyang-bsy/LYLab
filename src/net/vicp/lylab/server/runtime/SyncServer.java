package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
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
		try {
			if(this.lyTaskQueue == null) {
				lyTaskQueue = new LYTaskQueue();
				try {
					lyTaskQueue.setMaxQueue(CoreDef.config.getConfig("SyncServer").getInteger("maxQueue"));
				} catch (Exception e) { }
				try {
					lyTaskQueue.setMaxThread(CoreDef.config.getConfig("SyncServer").getInteger("maxThread"));
				} catch (Exception e) { }
			}
			serverSocket = new ServerSocket(CoreDef.config.getConfig("SyncServer").getInteger("port"));
			while (!isClosed.get()) {
				lyTaskQueue.addTask(new ToClientLongSocket(serverSocket, new SimpleHeartBeat()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		if(this.lyTaskQueue == null)
			this.lyTaskQueue = lyTaskQueue;
	}

}
