package net.vicp.lylab.utils.internet.test;

import java.net.ServerSocket;

import net.vicp.lylab.utils.internet.LYSocket;

public abstract class _LongSocket extends LYSocket {
	private static final long serialVersionUID = 5660256830220815074L;

	public _LongSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}
	
	public _LongSocket(String ip, int port) {
		super(ip, port);
		recycle(ip, port);
	}
	
}
