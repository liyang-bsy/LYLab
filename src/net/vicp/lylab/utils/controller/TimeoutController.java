package net.vicp.lylab.utils.controller;

import java.util.Map;
import java.util.WeakHashMap;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.tq.Task;

public final class TimeoutController extends Task {
	private static final long serialVersionUID = -4494667245957319328L;

	private static Boolean init = false;
	private static TimeoutController instance = null;

	private Map<Recyclable, Integer> watchList = new WeakHashMap<Recyclable, Integer>();
	
	@Override
	public boolean isDaemon() {
		return true;
	}
	
	@Override
	public void exec() {
		try {
			if (!init) {
				instance = this;
				init = true;
			}
			if (isStopped())
				return;
			while (!getThread().isInterrupted()) {
				if (isStopped())
					break;
				Thread.sleep(CoreDefine.WAITING);
				getInstance().timeoutControl();
			}
		} catch (InterruptedException e) {
			throw new LYException("TimeoutController is interrupted");
		} finally {
			instance = this;
			init = true;
		}
	}

	private void timeoutControl() {
		System.gc();
//		List<Task> callStopList = new LinkedList<Task>();
//		List<Task> forceStopList = new LinkedList<Task>();
		for(Recyclable rec : watchList.keySet())
		{
			// skip myself
			if(rec instanceof TimeoutController) continue;
			if(isStopped() && !rec.isRecyclable()) continue;
			rec.recycle();
		}
//		for(Task task : LYTaskQueue.getThreadPool())
//		{
//			// skip WatchDog itself
//			if(task instanceof WatchDog) continue;
//			if(isStopped()) continue;
//			// -1L means infinite
//			if()
//				continue;
//		}
//		for(Task task : forceStopList)
//		{
//			task.forceStop();
//			if(task.getRetryCount() > 0)
//				task.recycle();
//			else log.error("Timeout task was killed:\n" + task.toString());
//		}
//		for(Task task : callStopList)
//		{
//			task.callStop();
//			log.info("Try to stop timeout task:" + task.getTaskId());
//		}
	}
	
	public synchronized static boolean addToWatch(Recyclable rec)
	{
		if(getInstance().getState() == Task.BEGAN)
			getInstance().begin();
		return getInstance().getWatchList().put(rec, CoreDefine.DEFAULT_TOLERANCE) != null;
	}

	public static TimeoutController getInstance() {
		return instance;
	}

	public static void setInstance(TimeoutController instance) {
		TimeoutController.instance = instance;
	}

	private Map<Recyclable, Integer> getWatchList() {
		return watchList;
	}

}
