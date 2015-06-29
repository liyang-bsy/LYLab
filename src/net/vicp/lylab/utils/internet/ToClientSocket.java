package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public abstract class ToClientSocket extends LYSocket {
	private static final long serialVersionUID = -2811199603276510531L;

	public ToClientSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
	}

	@Override
	abstract public byte[] response(byte[] request);

}
