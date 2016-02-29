package net.vicp.lylab.core.pool.plugins;

import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.pool.Pool;

public class TimeoutControlPlugin<T> extends AbstractPlugin<T> implements Recyclable {

	protected Pool<T> controller;
	
	public TimeoutControlPlugin(Pool<T> controller) {
		this.controller = controller;
	}
	
	@Override
	public boolean isRecyclable() {
		return false;
	}

	@Override
	public void recycle() {
	}
	
	// Plug-in information
	@Override
	public int priority() {
		return 0;
	}

	@Override
	public int layer() {
		return 1;
	}

}
