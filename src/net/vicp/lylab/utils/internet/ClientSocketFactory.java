package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

import net.vicp.lylab.utils.internet.test.TestSocket;
import net.vicp.lylab.utils.tq.Task;

public abstract class ClientSocketFactory extends Task {
	private static final long serialVersionUID = -2516954630546529661L;
	
	LYSocket longSocket;
	
	public ClientSocketFactory(ServerSocket serverSocket)
	{
		this.longSocket = new TestSocket(serverSocket);
	}

}
