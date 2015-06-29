package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.utils.internet.Protocol;
import net.vicp.lylab.utils.internet.ToClientLongSocket;

public class TestToClientLongSocket<T> extends ToClientLongSocket<T> {
	private static final long serialVersionUID = 5845683298739007258L;

	public TestToClientLongSocket(ServerSocket serverSocket) {
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
