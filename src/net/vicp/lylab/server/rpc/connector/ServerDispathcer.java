package net.vicp.lylab.server.rpc.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public class ServerDispathcer extends NonCloneableBaseObject {
//	//		Server	Procedure	ServerAddress
//	DualMap<Pair<String, String>, String> servers;

	//					Server	Procedure
	protected final Map<String, Set<String>> server2procedure;
	//					Server	Address
	protected final Map<String, List<String>> server2addr;
	
	// random access
	protected final Random random = new Random();
	
	// mode
	protected boolean restrict = true;
	
	public ServerDispathcer() {
		server2procedure = new HashMap<>();
		server2addr = new HashMap<>();
	}
	
	public String getAddress(String server, String procedure) {
		List<String> ipList = server2addr.get(server);
		if (ipList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		int seq = random.nextInt(ipList.size());
		return ipList.get(seq);
	}
	
	public List<String> getAllAddress(String server, String procedure) {
		List<String> ipList = server2addr.get(server);
		if (ipList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		return ipList;
	}

	public void addServer(String server, String ip) {
		synchronized (lock) {
			addServer(server);
			server2addr.get(server).add(ip);
		}
	}

	private void addServer(String server) {
		synchronized (lock) {
			if (server2addr.get(server) == null)
				server2addr.put(server, new ArrayList<String>());
			if (server2procedure.get(server) == null)
				server2procedure.put(server, new HashSet<String>());
		}
	}

	public void addProcedure(String server, String procedure) {
		synchronized (lock) {
			if (server2addr.containsKey(server)) {
				server2procedure.get(server).add(procedure);
			}
		}
	}

	public void removeServer(String server, String ip) {
		synchronized (lock) {
			server2addr.remove(server);
			server2procedure.remove(server);
		}
	}

	public void removeProcedure(String server, String procedure) {
		synchronized (lock) {
			if (server2addr.containsKey(server)) {
				server2procedure.containsValue(server);
				Set<String> procedures = server2procedure.get(server);
				procedures.remove(procedure);
			}
		}
	}
	
}
