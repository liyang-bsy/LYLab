package net.vicp.lylab.utils.internet.async;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.server.aop.DoActionLong;
import net.vicp.lylab.utils.internet.impl.SimpleHeartBeat;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends Task implements LifeCycle{
	private static final long serialVersionUID = 883892527805494627L;
	protected volatile boolean running = true;
	protected ServerSocket serverSocket;
	protected LYTaskQueue tq;
	
	protected static SyncServer instance;
	
	protected SyncServer() { }
	
	@Override
	public void start() { }
	
	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public void exec() {
		try {
			int port = CoreDef.config.getInteger("port");
			serverSocket = new ServerSocket(port);
			while (running ) {
				tq.addTask(new DoActionLong(serverSocket, new SimpleHeartBeat()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SyncServer getInstance() {
		if(instance == null)
			synchronized (instance) {
				if(instance == null)
					instance = new SyncServer();
			}
		return instance;
	}

}
