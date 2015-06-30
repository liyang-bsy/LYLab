package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public abstract class ToClientSocket extends LYSocket {
	private static final long serialVersionUID = -5356816913222343651L;

	public ToClientSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
	}

	@Override
	abstract public byte[] response(byte[] request);

}
