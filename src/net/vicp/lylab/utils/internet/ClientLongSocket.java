package net.vicp.lylab.utils.internet;

public class ClientLongSocket extends LongSocket {
	private static final long serialVersionUID = -2811199603276510531L;

	public ClientLongSocket(String host, int port, HeartBeat heartBeat) {
		super(host, port, heartBeat);
	}

}
