package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

public class TestDealWithClientSocket extends ClientSocketFactory {
	private static final long serialVersionUID = -2516954630546529661L;
	
	public TestDealWithClientSocket(ServerSocket serverSocket)
	{
		super(serverSocket);
		System.out.println(serverSocket.getInetAddress().getHostAddress());
	}

	@Override
	public void exec() {
		try {
			while (true) {
//				MyData m = new MyData();
//				m.setValue("来自Server:");
				System.out.println("-收:" + Protocol.fromBytes(longSocket.receive()).transformData());
//				System.out.println("-发:\t" + m.encode().transformData());
//				longSocket.send(m.encode().toBytes());
				System.out.println("-发完");

				System.out.println("-等");
				Thread.sleep(3000L);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
