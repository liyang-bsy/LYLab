package net.vicp.lylab.utils.timer;

import java.util.Date;

/**
 * 	Extends InstantJob and reference to Plan(manage class).<br>
 * 	Override run() to satisfy your needs.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public abstract class InstantJob extends TimerJob {

	/**
	 * As an instant job, return 'now' to tell manager start right now.
	 */
	public Date getStartTime()
	{
		return new Date();
	}

}
