package net.vicp.lylab.utils.internet;

import java.net.Socket;

import net.vicp.lylab.utils.tq.Task;

public abstract class ClientSocketFactory extends Task {
	private static final long serialVersionUID = -2516954630546529661L;
	
	LongSocketClient longSocket;
	
	public ClientSocketFactory(Socket socket)
	{
		this.longSocket = new LongSocketClient(socket);
	}

}
