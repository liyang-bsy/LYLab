package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.utils.tq.LYTaskQueue;

public class TestServer extends BaseObject {
	private ServerSocket server;
	protected int port;

	public static void main(String[] args) throws Exception {
		System.out.println("Server begin");
		new TestServer(52041, true);
		
	}
	
	TestServer(int port, boolean isLongConnection) throws Exception {
		server = new ServerSocket(port);
		while(true)
		{
			if(isLongConnection)
				LYTaskQueue.addTask(new TestToClientLongSocket<MyData>(server));
			else
				LYTaskQueue.addTask(new TestToClientSocket(server));
		}
	}

}