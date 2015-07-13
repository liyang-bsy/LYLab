package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.Protocol;

/**
 * A client specific long socket.
 * You can use this to request foreigner server with long socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ClientLongSocket extends LongSocket {
	private static final long serialVersionUID = -2811199603276510531L;
	
	public ClientLongSocket(String host, Integer port, Protocol protocol, HeartBeat heartBeat) {
		super(host, port, protocol, heartBeat);
	}

}
