package net.vicp.lylab.utils.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Caster;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RDMAClient extends NonCloneableBaseObject implements LifeCycle {
	protected AutoGeneratePool<SyncSession> pool = null;
	protected AutoCreator<SyncSession> creator = null;
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected Protocol protocol;
	protected String rdmaHost;
	protected int rdmaPort;
	protected HeartBeat heartBeat;
	//

	@SuppressWarnings("unchecked")
	public List<Message> call(Message message, boolean broadcast) {
		message.setKey("RPC");

		List<Message> callResult = new ArrayList<>();
		Message retM = callRdmaServer(message);
		List<HashMap<String, Object>> list = ((List<HashMap<String, Object>>) retM.getBody().get("CallResult"));
		for (HashMap<String, Object> temp : list)
			callResult.add(Caster.map2Object(Message.class, (HashMap<String, Object>) temp.get("right")));
		return callResult;
	}

	public Message call(Message message) {
		return call(message, false).get(0);
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

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
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
