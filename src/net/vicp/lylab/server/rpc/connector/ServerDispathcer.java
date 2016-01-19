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
import net.vicp.lylab.core.model.Pair;

public class ServerDispathcer extends NonCloneableBaseObject {
	//					Server	Procedure
	protected final Map<String, Set<String>> server2procedure;
	//					Server		Address:ip	port
	protected final Map<String, List<Pair<String, Integer>>> server2addr;
	
	// random access
	protected final Random random = new Random();
	
	// mode
	protected boolean restrict = true;

	public ServerDispathcer() {
		server2procedure = new HashMap<>();
		server2addr = new HashMap<>();
	}

	public Pair<String, Integer> getRandomAddress(String server, String procedure) {
		List<Pair<String, Integer>> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		int seq = random.nextInt(addrList.size());
		return addrList.get(seq);
	}

	public List<Pair<String, Integer>> getAllAddress(String server, String procedure) {
		List<Pair<String, Integer>> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		return addrList;
	}

	public void addServer(String server, String ip, int port) {
		synchronized (lock) {
			addServer(server);
			server2addr.get(server).add(new Pair<>(ip, port));
		}
	}

	private void addServer(String server) {
		synchronized (lock) {
			if (server2addr.get(server) == null)
				server2addr.put(server, new ArrayList<Pair<String, Integer>>());
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
