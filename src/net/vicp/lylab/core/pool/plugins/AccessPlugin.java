package net.vicp.lylab.core.pool.plugins;

import net.vicp.lylab.core.exceptions.LYException;

public class AccessPlugin<T> extends AbstractPlugin<T> {

	protected StoragePlugin<T> partner;

	public AccessPlugin(StoragePlugin<T> partner) {
		this.partner = partner;
	}

	public T access() {
		if (partner instanceof DefaultStoragePlugin)
			return null;
		throw new LYException("This Access Plug-in only matches to " + DefaultStoragePlugin.class.getName());
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
