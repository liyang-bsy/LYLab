package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.interfaces.recycle.Recyclable;

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
