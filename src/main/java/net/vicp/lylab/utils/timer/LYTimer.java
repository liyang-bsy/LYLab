package net.vicp.lylab.utils.timer;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;

/**
 * 	LYPlan is a tiny schedule framework, could be apply to multitude purpose.
 *  <br><br>
 * 	Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public final class LYTimer extends NonCloneableBaseObject implements LifeCycle {
	private List<TimerJob> jobs = null;
	private AutoInitialize<Timer> timer = new AtomicStrongReference<Timer>();
	private AtomicBoolean closed = new AtomicBoolean(true);
	
	@Override
	public void initialize() {
		synchronized (lock) {
			if (!closed.getAndSet(false))
				return;
			for (TimerJob bj : getJobs()) {
				timer.get(Timer.class).schedule(bj.proxy, bj.getStartTime(), bj.getInterval());
				log.info("LYTimer - Load scheduled job: " + bj.getClass().getName());
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (closed.getAndSet(true))
				return;
			for (TimerJob tj : getJobs()) {
				tj.proxy.cancel();
				log.info("LYTimer - Cancel scheduled job: " + tj.getClass().getName());
			}
			timer.get(Timer.class).cancel();
		}
	}

	/**
	 * TimeJob will be cancelled before start(if possible).
	 * @param bj	TimeJob you want to start
	 * @return
	 */
	public boolean addTimeJob(TimerJob bj) {
		synchronized (lock) {
			if (!closed.get())
				throw new LYException("This timer hadn't initialized yet!");
			try {
				bj.proxy.cancel();
				jobs.add(bj);
				timer.get(Timer.class).schedule(bj.proxy, bj.getStartTime(), bj.getInterval());
				log.info("LYTimer - Load new schedule job: " + bj.getClass().getName());
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
				return false;
			}
			return true;
		}
	}

	public List<TimerJob> getJobs() {
		return jobs;
	}

	public void setJobs(List<TimerJob> jobs) {
		this.jobs = jobs;
	}

}
