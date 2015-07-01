package net.vicp.lylab.utils.internet.test;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

import net.vicp.lylab.core.interfaces.recycle.Refiller;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.atomic.AtomicLong;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.internet.ClientSocket;
import net.vicp.lylab.utils.internet.ConnectionPool;
import net.vicp.lylab.utils.internet.LYSocket;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

public class TestClient extends Task implements Refiller<LYSocket> {
	private static final long serialVersionUID = 4660521465950864362L;

	public static Config conf = new Config(System.getProperty("user.dir") + "\\config\\internetConfig.txt");
	
	private static ConnectionPool<LYSocket> cp;
	private static boolean isLong;

	public static AtomicLong totalRequestCount = new AtomicLong(0);
	public volatile static AtomicInteger[] access = new AtomicInteger[] {
		new AtomicInteger(),
	};
	public volatile static AtomicInteger[] accessF = new AtomicInteger[] {
		new AtomicInteger(),
	};
	
	@Override
	public LYSocket refill() {
		if(!isLong)
			return new ClientSocket(conf.getString("server"),conf.getInteger("port"));
		else
			return new ClientLongSocket(conf.getString("server"),conf.getInteger("port"), new LYHeartBeat());
	}
	
	public static void main(String[] arg) throws UnsupportedEncodingException, Exception
	{
		ProtocolUtils.setProtocolConfig(System.getProperty("user.dir") + "\\config\\protocol.txt");
		conf.load();
		boolean isL = conf.getBoolean("isLong");
		
		if(!isL)
			cp = new ConnectionPool<LYSocket>(new TestClient(isL),conf.getString("server"),conf.getInteger("port"),conf.getLong("timeout"),conf.getInteger("capacity"));
		else
			cp = new ConnectionPool<LYSocket>(new TestClient(isL),conf.getString("server"),conf.getInteger("port"),conf.getLong("timeout"),conf.getInteger("capacity"));
		for(int i=0;i<conf.getLong("thread");i++)
			LYTaskQueue.addTask(new TestClient(isL));
		

		for(int j = 0;j<Integer.MAX_VALUE;j+=1)
		{
			access[0].set(0);
			accessF[0].set(0);
			Thread.sleep(1*1000);
			System.out.println("second:" + j + "\t\ttotal:" + totalRequestCount.get() + "\t\taverage:" + new DecimalFormat("0.00").format(1.0*totalRequestCount.get()/(j)));
			System.out.println("L0:" + access[0].get() + "\tLF0:" + accessF[0].get());
		}
	}
	
	public TestClient(boolean isLong)
	{
		TestClient.isLong = isLong;
	}

	@Override
	public void exec() {
		LYSocket socket = null;
		if(!isLong)
		{
			socket = cp.accessOne();
			socket.request(new MyData("MyData:-1").encode().toBytes());
//			System.out.println(ProtocolUtils.fromBytes(ret));
		}
		else
		{
			while((socket = cp.accessOne())==null)
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			socket.begin();
			for(int i=0;i<conf.getLong("sendPackageCnt");i++)
			{
				((ClientLongSocket) socket).addToPool(new MyData("MyData:"+getTaskId() + "\ttimes:" + i));
			}
			try {
				Thread.sleep(30000L);
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(socket != null)
			cp.recycle(socket.getObjectId());
	}

}
