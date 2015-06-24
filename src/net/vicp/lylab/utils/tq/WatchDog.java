package net.vicp.lylab.utils.tq;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.exception.LYException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class WatchDog extends Task implements Runnable {

	private static final long serialVersionUID = -4494667245957319328L;

	protected Log log = LogFactory.getLog(getClass());

	private static Boolean init = false;
	private static Boolean isRunning = false;
	private static WatchDog instance = null;

	public static Integer DEFAULTINTERVAL = 2000;			// 2 second
	public static Long DEFAULTTOLERANCE = 5*1000L;			// 5 min
	private Integer interval = DEFAULTINTERVAL;
	private Long tolerance = DEFAULTTOLERANCE;
	
	private List<Task> forewarnList;
	
	@Override
	public boolean isDaemon()
	{
		return true;
	}
	
	@Override
	public void exec() {
		try {
			if (!init) {
				forewarnList = new ArrayList<Task>();
				instance = this;
				init = true;
			}
			if (isRunning)
				return;
			while (!getThread().isInterrupted()) {
				if (isStopped())
					break;
				Thread.sleep(interval);
				getInstance().timeoutControl();
			}
		} catch (InterruptedException e) {
			throw new LYException("WatchDog is interrupted");
		} finally {
			isRunning = false;
		}
	}

	@SuppressWarnings("deprecation")
	private void timeoutControl() {
		List<Task> callStopList = new LinkedList<Task>();
		List<Task> forceStopList = new LinkedList<Task>();
		for(Task task : LYTaskQueue.getThreadPool())
		{
			// skip WatchDog itself
			if(task instanceof WatchDog) continue;
			if(isStopped()) continue;
			// -1L means infinite
			if(task.getTimeout() == -1L || task.getState() == Task.COMPLETED || task.getState() == Task.FAILED)
				continue;
			if(task.getTimeout() + tolerance < new Date().getTime() - task.getStartTime().getTime())
				forceStopList.add(task);
			else if(task.getTimeout() < new Date().getTime() - task.getStartTime().getTime())
				callStopList.add(task);
		}
		for(Task task : forceStopList)
		{
			task.forceStop();
			if(task.getRetryCount() > 0)
			{
				task.recycle();
				LYTaskQueue.addTask(task);
			}
			else log.error("Timeout task was killed:\n" + task.toString());
		}
		for(Task task : callStopList)
		{
			task.callStop();
			log.info("Try to stop timeout task:" + task.getTaskId());
		}
	}

	public static WatchDog getInstance() {
		return instance;
	}

	public static void setInstance(WatchDog instance) {
		WatchDog.instance = instance;
	}

	@SuppressWarnings("deprecation")
	public static void killAll() {
		for(Task task : LYTaskQueue.getThreadPool())
			task.forceStop();
		getInstance().forceStop();
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

	public List<Task> getForewarnList() {
		List<Task> tmp = forewarnList;
		forewarnList = new ArrayList<Task>();
		return tmp;
	}
	
}
