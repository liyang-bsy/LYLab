package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
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
public class SyncServerSingleton extends Task implements LifeCycle, InitializeConfig {
	private static final long serialVersionUID = 883892527805494627L;
	protected volatile boolean running = true;
	protected ServerSocket serverSocket;
	protected LYTaskQueue tq;
	protected Config config;
	
	protected static SyncServerSingleton instance;
	
	protected SyncServerSingleton() { }
	
	@Override
	public void initialize() { }
	
	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public void exec() {
		try {
			serverSocket = new ServerSocket(config.getInteger("port"));
			while (running) {
				tq.addTask(new ToClientLongSocket(serverSocket, new SimpleHeartBeat()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SyncServerSingleton getInstance() {
		if(instance == null)
			synchronized (instance) {
				if(instance == null)
					instance = new SyncServerSingleton();
			}
		return instance;
	}

	@Override
	public void obtainConfig(Config config) {
		this.config = config;
	}

}
