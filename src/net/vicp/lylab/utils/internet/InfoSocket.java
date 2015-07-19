package net.vicp.lylab.utils.internet;

import java.net.Socket;

import net.vicp.lylab.utils.internet.async.BaseSocket;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class InfoSocket extends BaseSocket {
	private static final long serialVersionUID = 883892527805494627L;
	
	// Raw data source
	protected Socket socket;
	
	public InfoSocket(Socket socket) {
		this.socket = socket;
		this.host = socket.getInetAddress().getHostAddress();
		this.port = socket.getPort();
		this.setIsServer(true);
	}

	@Override
	public void exec() {
		// do nothing
	}

	@Override
	public byte[] doResponse(byte[] request) {
		// do nothing
		return null;
	}

}
