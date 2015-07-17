package net.vicp.lylab.core.interfaces;

import java.util.EventListener;

public interface Terminate extends EventListener {
	/**
	 * Call this when life cycle ended
	 */
	public void terminate();

}
