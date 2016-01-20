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
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RPCaller extends NonCloneableBaseObject implements LifeCycle {
	AutoGeneratePool<ClientLongSocket> pool = null;
	AutoCreator<ClientLongSocket> creator = null;

	public Message call(RPCMessage content) {
		if (StringUtils.isBlank(content.getRpcKey()))
			content.setRpcKey("RPC");
		Protocol p = (Protocol) CoreDef.config.getObject("protocol");
		ClientLongSocket cls = pool.accessOne();
		byte[] req, res;
		req = p.encode(content);
		res = cls.request(req);
		pool.recycle(cls);
		return (Message) p.decode(res);
	}

	@Override
	public void initialize() {
		creator = new InstanceCreator<ClientLongSocket>(ClientLongSocket.class, CoreDef.config.getString("rpcHost"),
				CoreDef.config.getInteger("rpcPort"), CoreDef.config.getObject("protocol"),
				CoreDef.config.getObject("heartBeat"));
		pool = new AutoGeneratePool<ClientLongSocket>(creator, new KeepAliveValidator<ClientLongSocket>(), 20000,
				Integer.MAX_VALUE);

		RPCMessage content = new RPCMessage();
		content.setRpcKey("RegisterServer");
		content.getBody().put("server", CoreDef.config.getString("server"));
		content.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
		Message m = call(content);
		if (m.getCode() != 0)
			throw new LYException("RPC Server register failed:\n" + m.toString());
	}

	@Override
	public void close() throws Exception {
		RPCMessage content = new RPCMessage();
		content.setRpcKey("RemoveServer");
		content.getBody().put("server", CoreDef.config.getString("server"));
		content.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
		call(content);

		pool.close();
	}

}
