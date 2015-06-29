package net.vicp.lylab.utils.internet;

public class ClientLongSocket<T> extends LongSocket<T> {
	private static final long serialVersionUID = -2811199603276510531L;

	public ClientLongSocket(String host, int port) {
		super(host, port);
	}

}
