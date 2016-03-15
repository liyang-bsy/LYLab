package net.vicp.lylab.utils.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Caster;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RPCClient extends NonCloneableBaseObject implements LifeCycle {
	
	public static void main(String[] args) {
		RPCClient client = new RPCClient();
		client.setProtocol(new LYLabProtocol());
		client.setRpcHost("127.0.0.1");
		client.setRpcPort(2001);
		client.setHeartBeat(new SimpleHeartBeat());
		client.setBackgroundServer(false);
		client.initialize();
		
		client.close();
	}
	
	protected AutoGeneratePool<SyncSession> pool = null;
	protected AutoCreator<SyncSession> creator = null;
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected Protocol protocol;
	protected String rpcHost;
	protected int rpcPort;
	protected HeartBeat heartBeat;
	//
	protected boolean backgroundServer = false;
	protected String serverName;
	protected int serverPort;

	@SuppressWarnings("unchecked")
	public List<Message> call(RPCMessage message, boolean broadcast) {
		message.setRpcKey("RPC");
		message.setBroadcast(broadcast);

		List<Message> callResult = new ArrayList<>();
		Message retM = callRpcServer(message);
		List<HashMap<String, Object>> list = ((List<HashMap<String, Object>>) retM.getBody().get("CallResult"));
		for (HashMap<String, Object> temp : list)
			callResult.add(Caster.map2Object(Message.class, (HashMap<String, Object>) temp.get("right")));
		return callResult;
	}

	public Message call(RPCMessage message) {
		return call(message, false).get(0);
	}

	public Message callRpcServer(RPCMessage message) {
		if (closed.get())
			throw new LYException("Client closed, did you initialize() Caller?");
		do {
			SyncSession session = pool.accessOne();
			try {
				byte[] req, res;
				req = protocol.encode(message);
				session.send(req);
				res = session.receive().getLeft();
				return (Message) protocol.decode(res);
			} catch (Exception e) {
				log.error("Communication with server failed, retry...");
			}
			finally {
				pool.recycle(session);
			}
		} while(true);
	}
	
	@Override
	public void initialize() {
		if (closed.compareAndSet(true, false)) {
			creator = new InstanceCreator<SyncSession>(SyncSession.class, rpcHost, rpcPort, protocol, heartBeat);
			pool = new AutoGeneratePool<SyncSession>(creator, new KeepAliveValidator<SyncSession>(), 20000,
					Integer.MAX_VALUE);

			if (isBackgroundServer()) {
				RPCMessage message = new RPCMessage();
				message.setRpcKey("RegisterServer");
				message.getBody().put("server", serverName);
				message.getBody().put("port", serverPort);
				Message m = callRpcServer(message);
				if (m.getCode() != 0)
					throw new LYException("RPC Server register failed:\n" + m.toString());
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (!closed.get()) {
				if (isBackgroundServer()) {
					RPCMessage message = new RPCMessage();
					message.setRpcKey("RemoveServer");
					message.getBody().put("server", serverName);
					message.getBody().put("port", serverPort);
					callRpcServer(message);
				}

				pool.close();
				closed.set(true);
			}
		}
	}

	public boolean isBackgroundServer() {
		return backgroundServer;
	}

	public void setBackgroundServer(boolean backgroundServer) {
		this.backgroundServer = backgroundServer;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public String getRpcHost() {
		return rpcHost;
	}

	public void setRpcHost(String rpcHost) {
		this.rpcHost = rpcHost;
	}

	public int getRpcPort() {
		return rpcPort;
	}

	public void setRpcPort(int rpcPort) {
		this.rpcPort = rpcPort;
	}

	public HeartBeat getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(HeartBeat heartBeat) {
		this.heartBeat = heartBeat;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

}
