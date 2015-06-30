package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.utils.internet.ToClientSocket;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

public class TestToClientSocket extends ToClientSocket {
	private static final long serialVersionUID = 5845683298739007258L;

	public TestToClientSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}

	@Override
	public byte[] response(byte[] request) {
		MyData m = (MyData) ProtocolUtils.fromBytes(request).toObject();
		return new MyData(m.getValue() + "\tTime:" + new Date().getTime()).encode().toBytes();
	}

}
