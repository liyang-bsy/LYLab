package net.vicp.lylab.core.pool.plugins;

import java.util.Collection;
import java.util.Map;

import net.vicp.lylab.core.pool.Pool;

public class DefaultStoragePlugin<T> extends StoragePlugin<T> {

	public DefaultStoragePlugin(Pool<T> controller) {
		super(controller);
	}

	// Storage
	protected Map<Long, T> container;
	protected volatile Collection<Long> keys;
	protected long lastId = 1L;

	/**
	 * Add an Object returns its id
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public Long add(T t) {
		long savedId = lastId;
		lastId++;
		container.put(savedId, t);
		return savedId;
	}

	/**
	 * Remove an Object returns itself
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public T remove(long objId) {
		T tmp = container.remove(objId);
		return tmp;
	}
	
	public int size() {
		return container.size();
	}

	public boolean isEmpty() {
		return container.isEmpty();
	}

	public void clear() {
		container.clear();
	}
	
}
