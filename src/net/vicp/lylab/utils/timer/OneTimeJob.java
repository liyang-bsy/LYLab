package net.vicp.lylab.utils.timer;

/**
 * 	Extends OneTimeJob and reference to Plan(manage class).<br>
 * 	Override run() to satisfy your needs.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public abstract class OneTimeJob extends TimerJob {

	/**
	 * As an one-time job, return '0' to tell manager it won't execute again.
	 */
	public Integer getInterval()
	{
		return 0;
	}

}
