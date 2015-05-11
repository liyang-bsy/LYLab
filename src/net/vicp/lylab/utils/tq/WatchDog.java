package net.vicp.lylab.utils.tq;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WatchDog extends Task implements Runnable {

	protected Log log = LogFactory.getLog(getClass());

	private static Boolean init = false;
	private static Boolean isRunning = false;
	private static WatchDog instance = null;

	static public final Integer DEFAULTINTERVAL = 2000;			// 2 second
	static public final Long DEFAULTTOLERANCE = 5*1000L;			// 5 min
	private Integer interval = DEFAULTINTERVAL;
	private Long tolerance = DEFAULTTOLERANCE;
	@Override
	public void exec() {
		if(!init)
		{
			instance = this;
			init = true;
		}
		if(isRunning)
			return;
		while(!getThread().isInterrupted())
		{
			if(isStopped()) break;
			try {
				Thread.sleep(interval);
				getInstance().timeoutControl();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void timeoutControl() {
		synchronized (LYTaskQueue.getThreadPool()) {
			for(Task task : LYTaskQueue.getThreadPool())
			{
				if(isStopped()) break;
				// 0 means infinite
				if(task.getTimeLimit() == 0L || task.getState() == Task.COMPLETED || task.getState() == Task.FAILED)
					continue;
				if(task.getTimeLimit() < new Date().getTime() - task.getStartTime().getTime())
				{
					task.callStop();
					log.info("Timeout detected:" + task.getTaskId());
				}
				if(task.getTimeLimit() + tolerance < new Date().getTime() - task.getStartTime().getTime())
				{
					task.getThread().stop(new TimeoutException());
					log.error("Timeout detected and a task was killed:\n" + task.toString());
				}
			}
		}
	}

	public static WatchDog getInstance() {
		return instance;
	}

	public static void setInstance(WatchDog instance) {
		WatchDog.instance = instance;
	}

	public static void stopWatchDog() {
		getInstance().callStop();
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Long getTolerance() {
		return tolerance;
	}

	public void setTolerance(Long tolerance) {
		this.tolerance = tolerance;
	}
	
}
