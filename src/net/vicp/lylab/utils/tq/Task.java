package net.vicp.lylab.utils.tq;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeoutException;

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
public abstract class Task implements Runnable, Executor, Cloneable, Serializable {

	private static final long serialVersionUID = -505125638835928043L;
	/**
	 * This value doesn't make any sense if you didn't use WatchDog
	 */
	protected Long timeout;
	protected Thread thread;
	
	protected Long taskId;
	protected volatile Integer state;
	protected Date startTime;
	
	static public Long DEFAULTTIMEOUT = 60*60*1000L;			// 1 hour
	
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
		timeout = DEFAULTTIMEOUT;
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
			while(state == STOPPED || state == BEGAN || state == STARTED)
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

	public Integer getState() {
		return state;
	}

	public Task setState(Integer state) {
		synchronized (this.state) {
			this.state = state;
		}
		return this;
	}

	public Thread getThread() {
		return thread;
	}

	public Task setThread(Thread thread) {
		this.thread = thread;
		return this;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Task setStartTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public Long getTimeout() {
		return timeout;
	}

	public Task setTimeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	public final synchronized void callStop() {
		this.setState(STOPPED);
		if(thread != null) thread.interrupt();
	}

	@Deprecated
	public final synchronized void forceStop() {
		synchronized (this) {
			this.notifyAll();
		}
		if(thread != null)
		{
			getThread().stop(new TimeoutException());
			LYTaskQueue.taskEnded(getTaskId());
		}
	}

	public Boolean isStopped() {
		return getState() == STOPPED;
	}
	
	@Override
	public String toString()
	{
		String sState = "UNKNOWN";
		switch (state) {
		case -2:
			sState = "STOPPED";
			break;
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
		return "taskId=" + taskId + ",className=" + getClass().getName() + ",state=" + sState + ",startTime=" + startTime + ",timeout=" + timeout;
	}

}
