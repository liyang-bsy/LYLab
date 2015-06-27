package net.vicp.lylab.utils.internet;

import java.net.Socket;

public class TestDealWithClientSocket extends ClientSocketFactory {
	private static final long serialVersionUID = -2516954630546529661L;
	
	public TestDealWithClientSocket(Socket socket)
	{
		super(socket);
		System.out.println(socket.getInetAddress().getHostAddress());
		System.out.println(socket.getPort());
	}

	@Override
	public void exec() {
		try {
			while (true) {
				System.out.println("-收:" + longSocket.receive());
				System.out.println("-发");
				longSocket.send("来自Server:".getBytes("UTF-8"));
				System.out.println("-发完");

				Thread.sleep(3000L);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
