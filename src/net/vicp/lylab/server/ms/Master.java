package net.vicp.lylab.server.ms;

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
			Collection<Servant> nodes) {
		this.hashMaker = hashMaker;
		this.numberOfReplicas = numberOfReplicas;

		for (Servant node : nodes) {
			add(node);
		}
	}

	public void add(Servant node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.put(hashMaker.hash(node.toString() + i), node);
		}
	}

	public void remove(Servant node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.remove(hashMaker.hash(node.toString() + i));
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
