package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.utils.internet.LongSocket;
import net.vicp.lylab.utils.internet.Protocol;

public class ToClientLongSocket<T> extends LongSocket<T> {
	private static final long serialVersionUID = -2811199603276510531L;

	public ToClientLongSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
	}

	@Override
	public byte[] response(byte[] request) {
		@SuppressWarnings("unchecked")
		T t = (T) Protocol.fromBytes(request).decodeJsonDataToObject();
		System.out.println(t);
		return new MyData("来自服务器:" + "\tTime:" + new Date().getTime()).encode().toBytes();
	}

}
