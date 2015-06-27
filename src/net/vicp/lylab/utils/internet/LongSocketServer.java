package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.utils.tq.Task;

public class LongSocketServer extends BaseObject {

	private ServerSocket server;
	
	protected int port;

	public static void main(String[] args) throws Exception {
		System.out.println("这是Server");
		new LongSocketServer(52041);
		
	}
	
	LongSocketServer(int port) throws Exception {
		server = new ServerSocket(port);
		while(true)
		{
			Task t = new TestDealWithClientSocket(server);
			t.begin("server client");
		}
	}

}