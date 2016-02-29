package net.vicp.lylab.core.pool.plugins;

import net.vicp.lylab.core.pool.Pool;

public abstract class StoragePlugin<T> extends AbstractPlugin<T> {

	protected Pool<T> controller;
	
	public StoragePlugin(Pool<T> controller) {
		this.controller = controller;
	}

	// Storage
	/**
	 * Add an Object returns its id
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public abstract Long add(T t);

	/**
	 * Remove an Object returns itself
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public abstract T remove(long objId);
	
	public abstract int size();

	public abstract boolean isEmpty();

	public abstract void clear();
	
	// Plug-in information
	@Override
	public int priority() {
		return 0;
	}

	@Override
	public int layer() {
		return 2;
	}

}
