package net.vicp.lylab.core.pool.plugins;

public class AccessPlugin<T> extends AbstractPlugin<T> {

	protected StoragePlugin<T> partner;
	
	public AccessPlugin(StoragePlugin<T> partner) {
		this.partner = partner;
	}

	public T access() {
		return null;
	}
	
	// Plug-in information
	@Override
	public int priority() {
		return 0;
	}

	@Override
	public int layer() {
		return 4;
	}

}
