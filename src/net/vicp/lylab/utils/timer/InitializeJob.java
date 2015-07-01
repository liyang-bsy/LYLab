package net.vicp.lylab.utils.timer;

import java.util.Date;

/**
 * 	Extends InitializeJob and reference to Plan(manage class).<br>
 * 	Override run() to satisfy your needs.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public abstract class InitializeJob extends InstantJob {

	/**
	 * As an instant job, return 'now' to tell manager start right now.
	 */
	public Date getStartTime()
	{
		return new Date();
	}

	/**
	 * As an one-time job, return '0' to tell manager it won't execute again.
	 */
	public Integer getInterval()
	{
		return 0;
	}

}
