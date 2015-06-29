package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.utils.internet.Protocol;
import net.vicp.lylab.utils.internet.ToClientSocket;

public class TestToClientSocket extends ToClientSocket {
	private static final long serialVersionUID = 5845683298739007258L;

	public TestToClientSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}

	@Override
	public byte[] response(byte[] request) {
		MyData m = (MyData) Protocol.fromBytes(request).decodeJsonDataToObject();
		return new MyData(m.getValue() + "\tTime:" + new Date().getTime()).encode().toBytes();
	}

}
