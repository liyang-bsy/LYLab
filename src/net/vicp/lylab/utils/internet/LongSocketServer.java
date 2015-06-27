package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;
import java.net.Socket;

import net.vicp.lylab.core.BaseObject;

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
			Socket socket = server.accept();
			new TestDealWithClientSocket(socket).begin("server client");
		}
	}

}