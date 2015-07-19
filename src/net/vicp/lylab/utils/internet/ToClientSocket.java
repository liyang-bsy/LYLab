package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

/**
 * A server specific socket.
 * You can use this to response foreigner client<tt>1</tt> time with auto-close socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ToClientSocket extends TaskSocket {
	private static final long serialVersionUID = -5356816913222343651L;

	public ToClientSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}

}
