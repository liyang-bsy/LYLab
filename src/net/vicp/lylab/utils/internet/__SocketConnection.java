package net.vicp.lylab.utils.internet;

import java.net.Socket;

import net.vicp.lylab.core.pool.TimeoutRecyclePool;
import net.vicp.lylab.utils.atomic.AtomicReference;

public class __SocketConnection  {
	
	TimeoutRecyclePool<AtomicReference<Socket>> sPool;
	String ip;
	Integer port;

	__SocketConnection()
	{
		sPool = new TimeoutRecyclePool<AtomicReference<Socket>>();
	}
	
	__SocketConnection(int maxSize)
	{
		sPool = new TimeoutRecyclePool<AtomicReference<Socket>>(maxSize);
	}
	
	__SocketConnection(long timoue, int maxSize)
	{
		sPool = new TimeoutRecyclePool<AtomicReference<Socket>>(timoue, maxSize);
	}
	
	public void init()
	{
		;
	}
	public AtomicReference<Socket> useSocket()
	{
		return sPool.accessOne();
	}
	
	public static void main(String[] a) throws Exception {
		__SocketConnection sc = new __SocketConnection(10);
		sc.init();
		AtomicReference<Socket> ref = sc.useSocket();
//		dosth(ref);
		Thread.sleep(10000L);
		long socketId = ref.getObjectId();
		sc.sPool.recycle(socketId);
		
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
