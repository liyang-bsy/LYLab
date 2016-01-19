package net.vicp.lylab.server.rpc.connector;

import java.util.HashMap;
import java.util.Map;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.core.pool.Pool;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class ServerConnector extends NonCloneableBaseObject {
	public Map<String, Pool<ClientLongSocket>> ip2ConnectionPool;
	public Map<String, AutoCreator<ClientLongSocket>> ip2Creator;
//	public AutoGeneratePool<ClientLongSocket> pool;
//	private AutoCreator<ClientLongSocket> creator;

	public ServerConnector() {
		ip2ConnectionPool = new HashMap<>();
		ip2Creator = new HashMap<>();
	}
	
	public ClientLongSocket getConnection(String ip, int port) {
		Pool<ClientLongSocket> pool = ip2ConnectionPool.get(ip);
		if (pool == null) {
			synchronized (lock) {
				pool = ip2ConnectionPool.get(ip);
				if (pool == null) {
					AutoCreator<ClientLongSocket> creator = new InstanceCreator<ClientLongSocket>(
							ClientLongSocket.class, ip, port, CoreDef.config.getObject("protocol"),
							CoreDef.config.getObject("heartBeat"));
					pool = new AutoGeneratePool<ClientLongSocket>(creator, new KeepAliveValidator<ClientLongSocket>(),
							20000, Integer.MAX_VALUE);
					ip2Creator.put(ip, creator);
					ip2ConnectionPool.put(ip, pool);
				}
			}
		}
		return ip2ConnectionPool.get(ip).accessOne();
	}

}
