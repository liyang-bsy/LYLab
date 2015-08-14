package net.vicp.lylab.utils.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
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
	private List<TimerJob> jobs = new ArrayList<TimerJob>();
	private AutoInitialize<Timer> timer = new AtomicStrongReference<Timer>();
	private AtomicBoolean init = new AtomicBoolean(false);
	
	@Override
	public void initialize() {
		synchronized (lock) {
			if(init.getAndSet(true)) return;
			for (String key : CoreDef.config.getConfig("LYTimer").keyList()) {
				try {
					Object tmp = CoreDef.config.getConfig("LYTimer").getObject(key);
					if(tmp instanceof String)
						jobs.add((TimerJob) CoreDef.config.getConfig("LYTimer").getNewInstance(key));
					if(tmp instanceof TimerJob)
						jobs.add((TimerJob) tmp);
				} catch (Exception e) {
					log.error("Failed to create timejob for key[" + key + "]"
							+ Utils.getStringFromException(e));
				}
			}
			for (TimerJob bj : getJobs()) {
				if (bj.getInterval() != 0)
					timer.get(Timer.class).schedule(bj, bj.getStartTime(), bj.getInterval());
				else
					timer.get(Timer.class).schedule(bj, bj.getStartTime());
				log.info("LYTimer - Load scheduled job: " + bj.getClass().getName());
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			for (TimerJob tj : getJobs()) {
				tj.cancel();
				log.info("LYTimer - Cancel scheduled job: " + tj.getClass().getName());
			}
			init.set(false);
			timer.get(Timer.class).cancel();
		}
	}

	/**
	 * TimeJob will be cancelled before start(if possible).
	 * @param bj	TimeJob you want to start
	 * @return
	 */
	public boolean NewTimeJob(TimerJob bj) {
		synchronized (lock) {
			try {
				bj.cancel();
				jobs.add(bj);
				if (bj.getInterval() != 0)
					timer.get(Timer.class).schedule(bj, bj.getStartTime(), bj.getInterval());
				else
					timer.get(Timer.class).schedule(bj, bj.getStartTime());
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

}
