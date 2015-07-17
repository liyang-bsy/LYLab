package net.vicp.lylab.core.interfaces;

import java.util.EventListener;

public interface Initialize extends EventListener {
	/**
	 * Call this when life cycle started
	 */
	public void initialize();

}
