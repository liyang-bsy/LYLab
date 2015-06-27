package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public class TestSocket extends LYSocket {
	private static final long serialVersionUID = 1600438374173859309L;
	
	public TestSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}

	public TestSocket(String ip, int port) {
		super(ip, port);
	}

	@Override
	public boolean response(byte[] request) {
		return true;
	}

}
