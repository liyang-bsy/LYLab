package net.vicp.lylab.utils.internet.test;

import java.io.UnsupportedEncodingException;

import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.internet.ClientSocket;
import net.vicp.lylab.utils.internet.LYSocket;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

public class TestClient extends ClientSocket {
	private static final long serialVersionUID = 4660521465950864362L;

	@SuppressWarnings("resource")
	public static void main(String[] arg) throws UnsupportedEncodingException, Exception
	{
		ProtocolUtils.setProtocolConfig(System.getProperty("user.dir") + "\\config\\protocol.txt");
		boolean isL = true;
		if(!isL)
		{
			System.out.println("one time");
			LYSocket socket = new ClientSocket("127.0.0.1",52041);
			byte[] ret = socket.request(new MyData("MyData:-1").encode().toBytes());
			System.out.println(ProtocolUtils.fromBytes(ret));
		}
		else
		{
			System.out.println("long socket");
			ClientLongSocket socket = new ClientLongSocket("127.0.0.1", 52041, new LYHeartBeat());
			socket.begin();
			for(int i=0;i<4;i++)
				socket.addToPool(new MyData("MyData:"+i));
			Thread.sleep(50000L);
			socket.close();
		}
	}

	public TestClient(String host, int port) {
		super(host, port);
	}

}
