package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.interfaces.recycle.Recyclable;

/**
 * A client specific socket.
 * You can use this to request foreigner server <tt>1</tt> time with auto-close socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ClientSocket extends LYSocket implements Recyclable, AutoCloseable, Transmission {
	private static final long serialVersionUID = 7043024251356229037L;

	public ClientSocket(String host, Integer port) {
		super(host, port);
	}

	@Override
	public byte[] request(byte[] request) {
		if(isServer()) return null;
		byte[] ret = null;
		connect();
		try {
			send(request);
			ret = receive();
			close();
		} catch (Exception e) { }
		return ret;
	}

}
