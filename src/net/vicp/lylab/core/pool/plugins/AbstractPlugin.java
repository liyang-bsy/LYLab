package net.vicp.lylab.core.pool.plugins;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Plugin;
import net.vicp.lylab.core.pool.Pool;
import net.vicp.lylab.utils.atomic.AtomicBoolean;

/**
 * Abstract Plugin
 * 
 * @author liyang
 *
 */
public abstract class AbstractPlugin<T> extends CloneableBaseObject implements Plugin {

	protected Pool<T> controller;
	protected Plugin partner;

	private AtomicBoolean close = new AtomicBoolean(false);

	public boolean isClose() {
		return close.get();
	}

	@Override
	public void initialize() {
		if (!close.compareAndSet(false, true))
			throw new LYException("This plugin is initialized");
	}

	@Override
	public void close() throws Exception {
		if (!close.compareAndSet(true, false))
			return;
	}

}
