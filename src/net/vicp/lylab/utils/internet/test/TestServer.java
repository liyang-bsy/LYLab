package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.LYTaskQueue;

public class TestServer extends BaseObject {
	private ServerSocket server;
	protected int port;
	
	public static Config conf = new Config(System.getProperty("user.dir") + "\\config\\internetConfig.txt");

	public static void main(String[] args) throws Exception {
		System.out.println("Server begin");
		ProtocolUtils.setProtocolConfig(System.getProperty("user.dir") + "\\config\\protocol.txt");
		new TestServer(52041, true);
		
	}
	
	TestServer(int port, boolean isLongConnection) throws Exception {
		server = new ServerSocket(port);
		while(true)
		{
			if(isLongConnection)
				LYTaskQueue.addTask(new TestToClientLongSocket(server, new LYHeartBeat()));
			else
				LYTaskQueue.addTask(new TestToClientSocket(server));
		}
	}

}
