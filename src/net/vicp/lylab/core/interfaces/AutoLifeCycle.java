package net.vicp.lylab.core.interfaces;

import java.util.EventListener;

public interface AutoLifeCycle extends EventListener {
	/**
	 * Call this when life cycle started
	 */
	public void initialize();
	/**
	 * Call this when life cycle ended
	 */
	public void terminate();

}
