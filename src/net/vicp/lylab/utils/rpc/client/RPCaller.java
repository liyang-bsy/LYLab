package net.vicp.lylab.utils.rpc.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Caster;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RPCaller extends NonCloneableBaseObject implements LifeCycle {
	private AutoGeneratePool<SyncSession> pool = null;
	private AutoCreator<SyncSession> creator = null;
	private boolean backgroundServer = false;
	private AtomicBoolean closed = new AtomicBoolean(true);
	Protocol protocol;

	@SuppressWarnings("unchecked")
	public List<Message> call(RPCMessage message, boolean broadcast) {
		message.setRpcKey("RPC");
		message.setBroadcast(broadcast);
		
		List<Message> callResult = new ArrayList<>();
		Message retM = callRpcServer(message);
//		callResult = (List<Message>) retM.getBody().get("CallResult");
		List<HashMap<String, Object>> list = ((List<HashMap<String, Object>>) retM.getBody().get("CallResult"));
		for (HashMap<String, Object> temp : list)
			callResult.add(Caster.map2Object(Message.class, (HashMap<String, Object>) temp.get("right")));
		return callResult;
	}
	
	public Message call(RPCMessage message) {
		return call(message, false).get(0);
	}
	
	public Message callRpcServer(RPCMessage message) {
		if(closed.get())
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
			creator = new InstanceCreator<SyncSession>(SyncSession.class, CoreDef.config.getString("rpcHost"),
					CoreDef.config.getInteger("rpcPort"), CoreDef.config.getObject("protocol"), CoreDef.config.getObject("heartBeat"));
			pool = new AutoGeneratePool<SyncSession>(creator, new KeepAliveValidator<SyncSession>(), 20000,
					Integer.MAX_VALUE);

			if (isBackgroundServer()) {
				RPCMessage message = new RPCMessage();
				message.setRpcKey("RegisterServer");
				message.getBody().put("server", CoreDef.config.getString("server"));
				message.getBody().put("port", CoreDef.config.getInteger("port"));
				message.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
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

}
