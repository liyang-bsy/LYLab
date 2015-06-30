package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;
import java.util.Date;

import net.vicp.lylab.core.TranscodeObject;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.internet.protocol.Protocol;

public class TestToClientLongSocket extends ToClientLongSocket {
	private static final long serialVersionUID = 5845683298739007258L;

	public TestToClientLongSocket(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket, heartBeat);
	}

	@Override
	public byte[] response(byte[] request) {
		TranscodeObject t = Protocol.fromBytes(request).decodeJsonDataToObject();
		System.out.println(t);
		return new MyData("来自服务器:" + "\tTime:" + new Date().getTime()).encode().toBytes();
	}

}
