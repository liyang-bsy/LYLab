package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

/**
 * A server specific long socket.
 * You can use this to response foreigner client with long socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ToClientLongSocket extends LongSocket {
	private static final long serialVersionUID = -1781713514926105187L;

	public ToClientLongSocket(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket, heartBeat);
	}

}
