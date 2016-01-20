package net.vicp.lylab.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.vicp.lylab.core.CloneableBaseObject;

public class DualMap<K, V> extends CloneableBaseObject implements Map<K, V> {
	public final Map<K, V> kvMap;
	public final Map<V, K> vkMap;

	public DualMap() {
		kvMap = new HashMap<>();
		vkMap = new HashMap<>();
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

	public K getKey(Object key) {
		return vkMap.get(key);
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
			vkMap.put(value, key);
			return kvMap.put(key, value);
		}
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (lock) {
			kvMap.putAll(m);
			for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
				vkMap.put(e.getValue(), e.getKey());
		}
	}

	public V remove(Object key) {
		synchronized (lock) {
			V v = kvMap.remove(key);
			vkMap.remove(v);
			return v;
		}
	}

	public K removeValue(Object value) {
		synchronized (lock) {
			K k = vkMap.remove(value);
			kvMap.remove(k);
			return k;
		}
	}

	public int size() {
		return kvMap.size();
	}

	public Collection<V> values() {
		return kvMap.values();
	}

	public Collection<K> keys() {
		return vkMap.values();
	}

	@Override
	public String toString() {
		return "DualIndexMap [map=" + kvMap + "]";
	}

}
