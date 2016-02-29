package net.vicp.lylab.core.pool.plugins;

import net.vicp.lylab.core.CoreDef;

public class SizePlugin<T> extends AbstractPlugin<T> {

	protected StoragePlugin<T> partner;
	
	public SizePlugin(StoragePlugin<T> partner) {
		this.partner = partner;
	}

	protected int maxSize = CoreDef.DEFAULT_CONTAINER_MAX_SIZE;
	
	public boolean isFull() {
		return partner.size() >= maxSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	// Plug-in information
	@Override
	public int priority() {
		return 0;
	}

	@Override
	public int layer() {
		return 3;
	}

}
