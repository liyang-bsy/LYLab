package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public class ToClientLongSocket<T> extends LongSocket<T> {
	private static final long serialVersionUID = -2811199603276510531L;

	public ToClientLongSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
	}

}
