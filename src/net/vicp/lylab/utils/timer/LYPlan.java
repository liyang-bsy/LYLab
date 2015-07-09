package net.vicp.lylab.utils.timer;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.LifeCycle;
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
public final class LYPlan extends NonCloneableBaseObject implements LifeCycle {

	private TimerJob[] jobs = null;
	
	private AtomicBoolean Scheduled = new AtomicBoolean(false);
	
	private static AutoInitialize<LYPlan> instance = new AtomicStrongReference<LYPlan>();

	@Override
	public void initialize() {
		log.info("LYPlan - Initialization started");
		getInstance().BeginSchedule();
	}

	@Override
	public void terminate() {
		for (TimerJob tj : getInstance().getJobs()) {
			tj.cancel();
			log.info("LYPlan - Cancel scheduled job: " + tj.getClass().getName());
		}
		Scheduled.set(false);
	}
	
	public void BeginSchedule() {
		if (!Scheduled.compareAndSet(false, true))
			return;
		Timer timer = new Timer();
		for (TimerJob bj : this.getJobs()) {
			if (bj.getInterval() != 0)
				timer.schedule(bj, bj.getStartTime(), bj.getInterval());
			else
				timer.schedule(bj, bj.getStartTime());
			log.info("LYPlan - Load scheduled job: " + bj.getClass().getName());
		}
	}

	/**
	 * TimeJob will be cancelled before start(if possible).
	 * @param bj	TimeJob you want to start
	 * @return
	 */
	public boolean NewTimeJob(TimerJob bj) {
		try {
			bj.cancel();
			Timer timer = new Timer();
			if (bj.getInterval() != 0)
				timer.schedule(bj, bj.getStartTime(), bj.getInterval());
			else
				timer.schedule(bj, bj.getStartTime());
			log.info("LYPlan - Load new schedule job: "
					+ bj.getClass().getName());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public TimerJob[] getJobs() {
		return jobs;
	}
	
	public void setJobs(TimerJob[] jobs) {
		this.jobs = jobs;
	}

	public static LYPlan getInstance() {
		return instance.get(LYPlan.class);
	}

}
