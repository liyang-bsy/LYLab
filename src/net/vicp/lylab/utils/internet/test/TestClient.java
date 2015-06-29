package net.vicp.lylab.utils.internet.test;

import java.io.UnsupportedEncodingException;

import net.vicp.lylab.utils.internet.LYSocket;
import net.vicp.lylab.utils.internet.LongSocket;
import net.vicp.lylab.utils.internet.Protocol;
import net.vicp.lylab.utils.internet.TaskSocket;

public class TestClient extends TaskSocket {
	private static final long serialVersionUID = 4660521465950864362L;

	@SuppressWarnings("resource")
	public static void main(String[] arg) throws UnsupportedEncodingException, Exception
	{
		boolean isL = true;
		if(!isL)
		{
			System.out.println("one time");
			LYSocket socket = new TaskSocket("127.0.0.1",52041);
			byte[] ret = socket.request(new MyData("MyData:-1").encode().toBytes());
			System.out.println(Protocol.fromBytes(ret));
		}
		else
		{
			System.out.println("long socket");
			LongSocket<MyData> socket = new LongSocket<MyData>("127.0.0.1",52041);
			socket.begin();
			for(int i=0;i<4;i++)
				socket.addToPool(new MyData("MyData:"+i));
			Thread.sleep(50000L);
			socket.close();
		}
	}

	public TestClient(String host, int port) {
		super(host, port);
		// TODO Auto-generated constructor stub
	}

}
