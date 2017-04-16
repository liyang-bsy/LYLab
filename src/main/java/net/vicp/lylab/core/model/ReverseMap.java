package net.vicp.lylab.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CloneableBaseObject;

public class ReverseMap<K, V> extends CloneableBaseObject implements Map<K, V> {
	public final Map<K, V> kvMap;
	public final Map<V, Collection<K>> vkMap;

	public ReverseMap() {
		kvMap = new ConcurrentHashMap<>();
		vkMap = new ConcurrentHashMap<>();
	}

	public void clear() {
		synchronized (lock) {
			kvMap.clear();
			vkMap.clear();
		}
	}

	public boolean containsKey(Object key) {
		return kvMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return vkMap.containsKey(value);
	}

	public Set<Entry<K, V>> entrySet() {
		return kvMap.entrySet();
	}

	public boolean equals(Object o) {
		return kvMap.equals(o) || vkMap.equals(o);
	}

	public V get(Object key) {
		return kvMap.get(key);
	}

	public int hashCode() {
		return kvMap.hashCode();
	}

	public boolean isEmpty() {
		return kvMap.isEmpty();
	}

	public Set<K> keySet() {
		return kvMap.keySet();
	}

	public Set<V> ValueSet() {
		return vkMap.keySet();
	}

	public V put(K key, V value) {
		synchronized (lock) {
			Collection<K> kc = vkMap.get(value);
			if (kc == null)
				kc = new ArrayList<>();
			kc.add(key);
			vkMap.put(value, kc);
			return kvMap.put(key, value);
		}
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (lock) {
			kvMap.putAll(m);
			for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
				Collection<K> kc = vkMap.get(e.getValue());
				if (kc == null)
					kc = new ArrayList<>();
				kc.add(e.getKey());
				vkMap.put(e.getValue(), kc);
			}
		}
	}

	public V remove(Object key) {
		synchronized (lock) {
			V v = kvMap.remove(key);
			Collection<K> kc = vkMap.get(v);
			kc.remove(key);
			return v;
		}
	}

	public void removeValue(Object value) {
		synchronized (lock) {
			Collection<K> kc = vkMap.remove(value);
			for (K k : kc)
				kvMap.remove(k);
			return;
		}
	}

	public int size() {
		return kvMap.size();
	}

	public Collection<V> values() {
		return kvMap.values();
	}

	public Collection<K> keys() {
		List<K> kc = new ArrayList<>();
		for (Collection<K> kl : vkMap.values())
			kc.addAll(kl);
		return kc;
	}

	@Override
	public String toString() {
		return "DualIndexMap [map=" + kvMap + "]";
	}

}
