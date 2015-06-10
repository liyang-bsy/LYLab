package net.vicp.lylab.utils.cache;

import java.util.Date;

import net.vicp.lylab.utils.timer.TimerJob;

public class FlushCache extends TimerJob {
	
	@Override
	public Integer getInterval() {
		return 5*MINUTE;
	}

	@Override
	public Date getStartTime() {
		return new Date();
	}

	@Override
	public void run() {
		LYCache.flush();
	}
	
}
