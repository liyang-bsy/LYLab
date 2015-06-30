package net.vicp.lylab.utils.tq;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
import net.vicp.lylab.utils.controller.TimeoutController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manager class to recycle tasks in thread pool(Which should be in running).<br>
 * Kill & Restart tasks within certain timeout.<br>
 * 
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.26
 * @version 2.0.0
 * 
 */
public final class WatchDog extends NonCloneableBaseObject implements Recyclable {
	protected Log log = LogFactory.getLog(getClass());

	private static AutoInitialize<WatchDog> instance = new AtomicStrongReference<WatchDog>();

	private Long tolerance = CoreDef.WAITING_TOLERANCE;

	/**
	 * WatchDog is always recyclable unless LYTaskQueue call off it
	 */
	@Override
	public boolean isRecyclable()
	{
		return true;
	}

	/**
	 * Major cycle to recycle tasks in running
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void recycle() {
		List<Task> callStopList = new LinkedList<Task>();
		List<Task> forceStopList = new LinkedList<Task>();
		boolean finished = false;
		do {
			Iterator<Task> iterator = LYTaskQueue.getInstance().getThreadPool().iterator();
			try {
				while (iterator.hasNext()) {
					Task task = iterator.next();
					// -1L means infinite
					if (task.getTimeout() == -1L)
						continue;
					if(task.getStartTime() == null)
						task.setStartTime(new Date());
					if (task.getTimeout() + tolerance < new Date().getTime() - task.getStartTime().getTime())
						forceStopList.add(task);
					else if (task.getTimeout() < new Date().getTime() - task.getStartTime().getTime())
						callStopList.add(task);
				}
				finished = true;
			} catch (NullPointerException | ConcurrentModificationException e) { }
		} while (finished);
		for(Task task : forceStopList)
		{
			task.forceStop();
			if(task.getRetryCount() > 0)
			{
				log.error("Timeout task was killed, but this task requested retry(" + task.getRetryCount() + "):\n" + task.toString());
				task.setRetryCount(task.getRetryCount() - 1);
				task.reset();
				LYTaskQueue.addTask(task);
			}
		}
		for(Task task : callStopList)
		{
			task.callStop();
			log.info("Try to stop timeout task:" + task.getTaskId());
		}
	}

	/**
	 * You should better never call this, to killAll tasks in running
	 */
	@SuppressWarnings("deprecation")
	public static void killAll() {
		for(Task task : LYTaskQueue.getInstance().getThreadPool())
			task.forceStop();
	}

	/**
	 * Call off WatchDog
	 */
	public static void stopWatchDog() {
		TimeoutController.removeFromWatch(getInstance());
	}

	/**
	 * Turn on WatchDog
	 */
	public static void startWatchDog() {
		TimeoutController.addToWatch(getInstance());
	}

	/**
	 * Auto generate itself (thread-safe)
	 * @return
	 */
	public static WatchDog getInstance() {
		return instance.get(WatchDog.class);
	}
	
	// getters & setters below
	public Long getTolerance() {
		return tolerance;
	}

	public void setTolerance(Long tolerance) {
		this.tolerance = tolerance;
	}
	
}
