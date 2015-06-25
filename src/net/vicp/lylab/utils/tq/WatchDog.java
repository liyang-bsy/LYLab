package net.vicp.lylab.utils.tq;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.atomic.AtomicReference;
import net.vicp.lylab.utils.atomic.AtomicSoftReference;
import net.vicp.lylab.utils.controller.TimeoutController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class WatchDog extends BaseObject implements Recyclable {
	protected Log log = LogFactory.getLog(getClass());

	private static AtomicReference<WatchDog> instance = new AtomicSoftReference<WatchDog>();

	private Long interval = CoreDefine.INTERVAL;
	private Long tolerance = CoreDefine.WAITING_TOLERANCE;
	
	private List<Task> forewarnList = new ArrayList<Task>();
	
	@Override
	public boolean isRecyclable()
	{
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void recycle() {
		List<Task> callStopList = new LinkedList<Task>();
		List<Task> forceStopList = new LinkedList<Task>();
		for(Task task : LYTaskQueue.getThreadPool())
		{
			// -1L means infinite
			if(task.getTimeout() == -1L)
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
				log.error("Timeout task was killed, but this task requested retry(" + task.getRetryCount() + "):\n" + task.toString());
				task.setRetryCount(task.getRetryCount() - 1);
				task.reset();
				LYTaskQueue.addTask(task);
				forewarnList.add(task);
			}
			else log.error("Timeout task was killed:\n" + task.toString());
		}
		for(Task task : callStopList)
		{
			task.callStop();
			log.info("Try to stop timeout task:" + task.getTaskId());
		}
	}

	@SuppressWarnings("deprecation")
	public static void killAll() {
		for(Task task : LYTaskQueue.getThreadPool())
			task.forceStop();
	}

	public static void stopWatchDog() {
		TimeoutController.removeFromWatch(getInstance());
	}
	
	public static void startWatchDog() {
		TimeoutController.addToWatch(getInstance());
	}

	public static WatchDog getInstance() {
		return instance.createInstance(WatchDog.class);
	}

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
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
