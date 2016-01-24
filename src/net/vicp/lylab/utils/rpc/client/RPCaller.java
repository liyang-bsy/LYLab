package net.vicp.lylab.utils.rpc.client;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RPCaller extends NonCloneableBaseObject implements LifeCycle {
	private AutoGeneratePool<SyncSession> pool = null;
	private AutoCreator<SyncSession> creator = null;
	private boolean backgroundServer = true;
	private AtomicBoolean closed = new AtomicBoolean(false);

	public Message call(RPCMessage message) {
		if (StringUtils.isBlank(message.getRpcKey()))
			message.setRpcKey("RPC");
		Protocol p = (Protocol) CoreDef.config.getObject("protocol");
		SyncSession session = pool.accessOne();
		byte[] req, res;
		req = p.encode(message);
		session.send(req);
		res = session.receive().getLeft();
		pool.recycle(session);
		return (Message) p.decode(res);
	}

	@Override
	public void initialize() {
		if (closed.compareAndSet(false, true)) {
			creator = new InstanceCreator<SyncSession>(SyncSession.class, CoreDef.config.getString("rpcHost"),
					CoreDef.config.getInteger("rpcPort"), CoreDef.config.getObject("protocol"),
					CoreDef.config.getObject("heartBeat"));
			pool = new AutoGeneratePool<SyncSession>(creator, new KeepAliveValidator<SyncSession>(), 20000,
					Integer.MAX_VALUE);

			if (isBackgroundServer()) {
				RPCMessage message = new RPCMessage();
				message.setRpcKey("RegisterServer");
				message.getBody().put("server", CoreDef.config.getString("server"));
				message.getBody().put("port", CoreDef.config.getInteger("port"));
				message.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
				Message m = call(message);
				if (m.getCode() != 0)
					throw new LYException("RPC Server register failed:\n" + m.toString());
			}
		}
	}

	@Override
	public void close() {
		if (closed.compareAndSet(true, false)) {
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

}
