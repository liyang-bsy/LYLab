package net.vicp.lylab.utils.timer;

import java.util.Date;
import java.util.TimerTask;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends TimerJob and reference to Plan(manage class).<br>
 * Override run() to satisfy your needs.<br>
 * 
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public abstract class TimerJob extends NonCloneableBaseObject implements Executor {

	TimerTaskProxy proxy = new TimerTaskProxy(this);
	/**
	 * Now every TimerJob may use this to log something
	 */
	protected transient static Logger log = LoggerFactory.getLogger(TimerJob.class);
	
	protected static final int ONE_TIME_TASK = 0;
	protected static final int MILLISECOND = 1;
	protected static final int SECOND = 1000 * MILLISECOND;
	protected static final int MINUTE = 60 * SECOND;
	protected static final int HOUR = 60 * MINUTE;
	protected static final int DAY = 24 * HOUR;
	protected static final int WEEK = 7 * DAY;

	/**
	 * Tell Plan when this job start to work.<br>
	 * If the date is past, run() will be called immediately.
	 * 
	 * @return Date that this schedule will be first called
	 */
	public abstract Date getStartTime();

	/**
	 * Tell Plan how long should this job work again.<br>
	 * i.e. this job will work repeatedly every a specific period since start
	 * time
	 * 
	 * @return ONE_TIME_TASK/n*MILLISECOND/n*SECOND/n*MINUTE/n*HOUR/n*DAY/n*WEEK<br>
	 *         For Example: 3*HOUR + 16*MINUTE
	 */
	public abstract Integer getInterval();

	public final void run() {
		try {
			exec();
		} catch (Throwable t) {
			log.error(Utils.getStringFromThrowable(t));
		}
	}

}

class TimerTaskProxy extends TimerTask {

	TimerJob timerJob;

	public TimerTaskProxy(TimerJob timerJob) {
		this.timerJob = timerJob;
	}

	@Override
	public void run() {
		if (timerJob != null)
			timerJob.run();
	}

}
