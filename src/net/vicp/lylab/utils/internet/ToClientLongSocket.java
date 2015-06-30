package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public class ToClientLongSocket extends LongSocket {
	private static final long serialVersionUID = -1781713514926105187L;

	public ToClientLongSocket(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket, heartBeat);
	}

}
