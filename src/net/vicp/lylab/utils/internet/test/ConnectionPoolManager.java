package net.vicp.lylab.utils.internet.test;

import java.net.Socket;

import net.vicp.lylab.core.pool.TimeoutRecyclePool;
import net.vicp.lylab.utils.atomic.AtomicReference;

public class ConnectionPoolManager  {
	
	TimeoutRecyclePool<AtomicReference<Socket>> cPool;
	String host;
	Integer port;

	ConnectionPoolManager()
	{
		cPool = new TimeoutRecyclePool<AtomicReference<Socket>>();
	}
	
	ConnectionPoolManager(int maxSize)
	{
		cPool = new TimeoutRecyclePool<AtomicReference<Socket>>(maxSize);
	}
	
	ConnectionPoolManager(long timoue, int maxSize)
	{
		cPool = new TimeoutRecyclePool<AtomicReference<Socket>>(timoue, maxSize);
	}
	
	public void init()
	{
		;
	}
	public AtomicReference<Socket> useSocket()
	{
		return cPool.accessOne();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
