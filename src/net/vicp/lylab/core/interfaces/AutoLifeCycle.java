package net.vicp.lylab.core.interfaces;

public interface AutoLifeCycle {
	/**
	 * Call this when life cycle started
	 */
	public void initialize();
	/**
	 * Call this when life cycle ended
	 */
	public void terminate();

}
