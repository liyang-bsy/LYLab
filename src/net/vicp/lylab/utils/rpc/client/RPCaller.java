package net.vicp.lylab.utils.rpc.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Caster;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RPCaller extends NonCloneableBaseObject implements LifeCycle {
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
	protected Config procedures;

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
		SyncSession session = pool.accessOne();
		byte[] req, res;
		req = protocol.encode(message);
		session.send(req);
		res = session.receive().getLeft();
		pool.recycle(session);
		return (Message) protocol.decode(res);
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
				message.getBody().put("procedures", procedures.keyList());
				Message m = callRpcServer(message);
				if (m.getCode() != 0)
					throw new LYException("RPC Server register failed:\n" + m.toString());
			}
		}
	}

	@Override
	public void close() {
		if (closed.compareAndSet(false, true)) {
			if (isBackgroundServer()) {
				RPCMessage message = new RPCMessage();
				message.setRpcKey("RemoveServer");
				message.getBody().put("server", CoreDef.config.getString("server"));
				message.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
				call(message);
			}

			pool.close();
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

	public Config getProcedures() {
		return procedures;
	}

	public void setProcedures(Config procedures) {
		this.procedures = procedures;
	}

}
