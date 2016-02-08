package net.vicp.lylab.utils.client;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.CacheMessageProtocol;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RDMAClient extends NonCloneableBaseObject implements LifeCycle {
	
	public static void main(String[] args) throws InterruptedException {
		RDMAClient client = new RDMAClient();
		client.setHeartBeat(new SimpleHeartBeat());
//		client.setProtocol(new CacheMessageProtocol());
		client.setRdmaHost("127.0.0.1");
		client.setRdmaPort(2050);
		client.initialize();
		
		client.set("a", "a".getBytes(), 1000);
		System.out.println("设置a，取值:" + new String(client.get("a")));
		Thread.sleep(990L);
		System.out.println("接近过期，取值并延期:" + new String(client.get("a", true)));
		Thread.sleep(990L);
		System.out.println("快过期了:" + new String(client.get("a", false)));
		Thread.sleep(11L);
		System.out.println("过期了:" + new String(client.get("a")));
		client.set("a", "b".getBytes());
		System.out.println("设置b，永不过期，取值:" + new String(client.get("a")));
		System.out.println("CAS结果：" + client.compareAndSet("a", "d".getBytes(), "c".getBytes()));
		System.out.println("CAS试c/设d，后取值:" + new String(client.get("a")));
		System.out.println("CAS结果：" + client.compareAndSet("a", "d".getBytes(), "b".getBytes()));
		System.out.println("CAS试b/设d，后取值:" + new String(client.get("a")));
		client.set("a", "e".getBytes());
		System.out.println("设置e，永不过期，取值:" + new String(client.get("a")));
		client.close();
	}
	
	protected AutoGeneratePool<SyncSession> pool = null;
	protected AutoCreator<SyncSession> creator = null;
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected static final Protocol protocol = new CacheMessageProtocol();
	protected String rdmaHost;
	protected int rdmaPort;
	protected HeartBeat heartBeat;

	public int set(String key, byte[] data) {
		return set(key, data, 0);
	}

	public int set(String key, byte[] data, int expireTime) {
		CacheMessage cm = new CacheMessage(0, "Set", key, data, false, expireTime);
		return callRdmaServer(cm).getCode();
	}

	public int compareAndSet(String key, byte[] data, byte[] cmpData) {
		return compareAndSet(key, data, cmpData, 0);
	}

	public int compareAndSet(String key, byte[] data, byte[] cmpData, int expireTime) {
		CacheMessage cm = new CacheMessage(0, "CompareAndSet", key, data, false, expireTime);
		cm.setCmpData(cmpData);
		return callRdmaServer(cm).getCode();
	}

	public byte[] get(String key) {
		return get(key, false);
	}

	public byte[] get(String key, boolean renew) {
		CacheMessage cm = new CacheMessage(0, "Get", key, new byte[0], renew, 0);
		return callRdmaServer(cm).getPair().getRight();
	}

	public byte[] delete(String key) {
		CacheMessage cm = new CacheMessage(0, "Delete", key, new byte[0], false, 0);
		return callRdmaServer(cm).getPair().getRight();
	}

	public CacheMessage callRdmaServer(CacheMessage message) {
		if (closed.get())
			throw new LYException("Client closed, did you initialize() Caller?");
		SyncSession session = pool.accessOne();
		byte[] req, res;
		req = protocol.encode(message);
		session.send(req);
		res = session.receive().getLeft();
		pool.recycle(session);
		return (CacheMessage) protocol.decode(res);
	}
	
	@Override
	public void initialize() {
		if (closed.compareAndSet(true, false)) {
			creator = new InstanceCreator<SyncSession>(SyncSession.class, rdmaHost, rdmaPort, protocol, heartBeat);
			pool = new AutoGeneratePool<SyncSession>(creator, new KeepAliveValidator<SyncSession>(), 20000,
					Integer.MAX_VALUE);
		}
	}

	@Override
	public void close() {
		if (closed.compareAndSet(false, true)) {
			pool.close();
		}
	}

	public String getRdmaHost() {
		return rdmaHost;
	}

	public void setRdmaHost(String rdmaHost) {
		this.rdmaHost = rdmaHost;
	}

	public int getRdmaPort() {
		return rdmaPort;
	}

	public void setRdmaPort(int rdmaPort) {
		this.rdmaPort = rdmaPort;
	}

	public HeartBeat getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(HeartBeat heartBeat) {
		this.heartBeat = heartBeat;
	}

}
