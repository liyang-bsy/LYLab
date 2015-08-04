package net.vicp.lylab.server.ms_give_up;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.DoHash;

public class Master extends NonCloneableBaseObject {
	Map<Integer, Servant> circle = new ConcurrentHashMap<Integer, Servant>();

	private DoHash hashMaker;
	private final int numberOfReplicas;

	public Master(DoHash hashMaker, int numberOfReplicas,
			Collection<Servant> servants) {
		this.hashMaker = hashMaker;
		this.numberOfReplicas = numberOfReplicas;

		for (Servant servant : servants) {
			add(servant);
		}
	}

	public void add(Servant servant) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.put(hashMaker.hash(servant.toString() + i), servant);
		}
	}

	public void remove(Servant servant) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.remove(hashMaker.hash(servant.toString() + i));
		}
	}

	public Servant get(String key) {
		if (circle.isEmpty()) {
			return null;
		}
		int hash = hashMaker.hash(key);
		return circle.get(hash);
	}
}
