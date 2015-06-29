package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.utils.internet.Protocol;
import net.vicp.lylab.utils.internet.TaskSocket;

public class ToClientSocket extends TaskSocket {
	private static final long serialVersionUID = -2811199603276510531L;

	public ToClientSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
	}

	@Override
	public byte[] response(byte[] request) {
		MyData m = (MyData) Protocol.fromBytes(request).decodeJsonDataToObject();
		return new MyData(m.getValue() + "\tTime:" + new Date().getTime()).encode().toBytes();
	}

}
