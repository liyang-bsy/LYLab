package net.vicp.lylab.utils.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
import net.vicp.lylab.utils.config.Config;

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
public final class LYTimer extends NonCloneableBaseObject implements LifeCycle, InitializeConfig {
	
	private static Config config;
	private static List<TimerJob> jobs = new ArrayList<TimerJob>();
	private static AutoInitialize<Timer> timer = new AtomicStrongReference<Timer>();
	private static AtomicBoolean Scheduled = new AtomicBoolean(false);
	private static AutoInitialize<LYTimer> instance = new AtomicStrongReference<LYTimer>();
	
	@Override
	public void initialize() {
		if(config == null) return;
		synchronized (lock) {
			if (!Scheduled.compareAndSet(false, true))
				return;
			for (String key : config.keySet()) {
				try {
					jobs.add((TimerJob) Class.forName(config.getString(key))
							.newInstance());
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
				log.info("LYPlan - Load scheduled job: " + bj.getClass().getName());
			}
		}
	}

	@Override
	public void terminate() {
		synchronized (lock) {
			for (TimerJob tj : getJobs()) {
				tj.cancel();
				log.info("LYPlan - Cancel scheduled job: " + tj.getClass().getName());
			}
			Scheduled.set(false);
			timer.get(Timer.class).cancel();
		}
	}

	@Override
	public void obtainConfig(Config config) {
		setConfig(config);
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
				log.info("LYPlan - Load new schedule job: " + bj.getClass().getName());
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
				return false;
			}
			return true;
		}
	}

	public static List<TimerJob> getJobs() {
		return jobs;
	}

	public static void setConfig(Config config) {
		LYTimer.config = config;
	}

	public static LYTimer getInstance() {
		return instance.get(LYTimer.class);
	}

}
