package net.vicp.lylab.utils.cache;

import net.vicp.lylab.utils.timer.InstantJob;

/**
 * Auto refresh cache system.
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.2
 */
public class RefreshCache extends InstantJob {
	
	@Override
	public Integer getInterval() {
		return 5*MINUTE;
	}

	@Override
	public void exec() {
		LYCache.flush();
	}
	
}
