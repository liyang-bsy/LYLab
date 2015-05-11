package net.vicp.lylab.utils.tq;

import java.util.Date;

import net.vicp.lylab.core.Executor;

/**
 * 	Extends Task and reference to TaskQueue(manage class).<br>
 * 	Override exec() to satisfy your needs.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.1
 * 
 */
public abstract class Task implements Runnable, Executor, Cloneable {

	/**
	 * This value doesn't make any sense if you didn't use WatchDog
	 */
	protected Long timeLimit;
	protected Thread thread;
	
	protected Long taskId;
	protected volatile Integer state;
	protected Date startTime;
	
	static public final Long DEFAULTTIMEOUT = 60*60*1000L;			// 1 hour
	static public final Integer STOPPED = -2;
	static public final Integer FAILED = -1;
	static public final Integer BEGAN = 0;
	static public final Integer STARTED = 1;
	static public final Integer COMPLETED = 2;
	static public final Integer CANCELLED = 3;

	public Task()
	{
		thread = null;
		startTime = null;
		timeLimit = DEFAULTTIMEOUT;
		taskId = null;
		state = BEGAN;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	public final void run()
	{
		synchronized (state) {
			if(state != BEGAN)
				return;
			state = STARTED;
			setStartTime(new Date());
		}
		try {
			exec();
			setState(COMPLETED);
		} catch (Throwable e) {
			e.printStackTrace();
			setState(FAILED);
		}

		synchronized (this) {
			this.notifyAll();
		}
		try {
			aftermath();
			LYTaskQueue.taskEnded(getTaskId());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * If you need do something when this task completed, override this.<br>
	 * It will execute unless this task was successfully CANCELLED
	 */
	public void aftermath()
	{
		return;
	}

	/**
	 * Alert! This function will block current thread!
	 * The task is finished when this function is completed.
	 * DO NOT use it with aftermath()
	 */
	public final void waitingForFinish() {
		synchronized (this)
		{
			while(getState() == STOPPED || getState() == BEGAN || getState() == STARTED)
			{
				try {
					this.wait(LYTaskQueue.getWaitingThreshold());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public final Long getTaskId() {
		return taskId;
	}

	public final Task setTaskId(Long taskId) {
		this.taskId = taskId;
		return this;
	}

	public synchronized Integer getState() {
		return state;
	}

	public synchronized Task setState(Integer state) {
		this.state = state;
		return this;
	}

	public synchronized Thread getThread() {
		return thread;
	}

	public synchronized Task setThread(Thread thread) {
		this.thread = thread;
		return this;
	}

	public Date getStartTime() {
		return startTime;
	}

	public synchronized Task setStartTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public Long getTimeLimit() {
		return timeLimit;
	}

	public synchronized Task setTimeLimit(Long timeLimit) {
		this.timeLimit = timeLimit;
		return this;
	}

	public final synchronized void callStop() {
		this.setState(STOPPED);
		if(thread != null) thread.interrupt();
	}

	public Boolean isStopped() {
		return getState() == STOPPED;
	}
	
	@Override
	public String toString()
	{
		String sState = "FAILED";
		switch (state) {
		case -1:
			sState = "FAILED";
			break;
		case 0:
			sState = "BEGAN";
			break;
		case 1:
			sState = "STARTED";
			break;
		case 2:
			sState = "COMPLETED";
			break;
		case 3:
			sState = "CANCELLED";
			break;
		default:
			break;
		}
		return "taskId=" + taskId + ",className=" + getClass().getName() + ",state=" + sState + ",startTime=" + startTime + ",timeLimit=" + timeLimit;
	}

}
