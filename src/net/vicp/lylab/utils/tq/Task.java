package net.vicp.lylab.utils.tq;

import java.io.Serializable;
import java.util.Date;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicInteger;

/**
 * Extends Task and reference to TaskQueue(manage class).<br>
 * Override exec() to satisfy your needs.<br>
 * 
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.26
 * @version 2.0.0
 * 
 */
public abstract class Task extends CloneableBaseObject implements Runnable, Executor, Serializable {
	private static final long serialVersionUID = -505125638835928043L;
	/**
	 * This value doesn't make any sense if you didn't use WatchDog
	 */
	protected long timeout = CoreDefine.DEFAULT_TASK_TTIMEOUT;
	/**
	 * How many time you want retry if this task was killed by WatchDog
	 */
	protected volatile Integer retryCount = 0;
	/**
	 * Control its running thread
	 */
	protected Thread thread;
	/**
	 * Report itself to LYTaskQueue
	 */
	private LYTaskQueue controller;

	/**
	 * Indicate when this task start run()
	 */
	protected Date startTime;

	private volatile AtomicInteger state = new AtomicInteger(BEGAN);

	static public final int STOPPED = -3;
	static public final int CANCELLED = -2;
	static public final int FAILED = -1;
	static public final int BEGAN = 0;
	static public final int STARTED = 1;
	static public final int COMPLETED = 2;

	public Task() {
		thread = null;
		startTime = null;
		state.set(BEGAN);
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	public final void run() {
		try {
			if (!state.compareAndSet(BEGAN, STARTED))
				return;
			setStartTime(new Date());
			exec();
			aftermath();
		} catch (Throwable e) {
			System.err.print(this.toString() + "\ncreated an error:\t" + Utils.getStringFromException(e));
			state.compareAndSet(STARTED, FAILED);
		} finally {
			state.compareAndSet(STARTED, COMPLETED);
			if(controller != null)
				controller.taskEnded(this);
			setThread(null);
		}
	}

	/**
	 * If you need do something when this task completed, override this.<br>
	 * It will execute unless this task was successfully CANCELLED<br>
	 * <br>
	 */
	protected void aftermath() {
		return;
	}

	/**
	 * Alert! This function will block current thread! The task is finished when
	 * this function is completed. DO NOT use it with aftermath()
	 * 
	 * @throws InterruptedException
	 */
	public final void join() throws InterruptedException {
		while (!join(CoreDefine.WAITING));
	}

	/**
	 * Alert! This function will block current thread! The task is finished when
	 * this function is completed. DO NOT use it with aftermath()
	 * 
	 * @throws InterruptedException
	 */
	public final boolean join(Long millis) throws InterruptedException {
		if (millis <= 0)
			throw new LYException("Timeout value must be positive");
		if (!isFinished())
		{
			thread.join(millis);
			return false;
		}
		return true;
	}
	
	/**
	 * Begin a task, if you put it into LYTaskQueue, then it will auto start
	 */
	public final void begin() {
		begin(null);
	}

	/**
	 * Begin a task yourself and assign a specific thread name to it
	 */
	public final void begin(String threadName) {
		if (state.get().intValue() != BEGAN || this.thread != null)
			return;
		setThread(new Thread(this));
		Thread t = getThread();
		if (threadName == null)
			t.setName("Task(" + getTaskId() + ") - " + getClass().getSimpleName() + "");
		else
			t.setName(threadName);
		t.setDaemon(isDaemon());
		t.start();
	}

	/**
	 * Try to call off the task, but can't ensure it will be end after calling this
	 */
	public final void callStop() {
		state.compareAndSet(STOPPED, STOPPED);
		state.compareAndSet(CANCELLED, STOPPED);
		state.compareAndSet(FAILED, STOPPED);
		state.compareAndSet(BEGAN, CANCELLED);
		state.compareAndSet(STARTED, STOPPED);
//		state.compareAndSet(COMPLETED, COMPLETED);		// non-sense
		if (thread != null)
			thread.interrupt();
	}

	/**
	 * If you found a task was lost itself in death loop or dead lock
	 */
	@Deprecated
	public final void forceStop() {
		callStop();
		if (thread != null) {
			getThread().stop(new LYException("Task " + getTaskId() + " timeout and killed"));
			thread = null;
		}
		if(controller != null)
			controller.taskEnded(this);
	}

	/**
	 * @return
	 * <tt>true</tt> if the task is stopped
	 */
	public Boolean isStopped() {
		return getState() == STOPPED || getState() == CANCELLED || getState() == FAILED;
	}

	/**
	 * @return
	 * <tt>true</tt> if the task is finished
	 */
	public Boolean isFinished() {
		return isStopped() || getState() == COMPLETED;
	}

	/**
	 * @return
	 * <tt>true</tt> if the task is a deamon
	 */
	protected boolean isDaemon() {
		return false;
	}

	// getters & setters below
	public final Long getTaskId() {
		return getObjectId();
	}

	public final Task setTaskId(Long taskId) {
		this.setObjectId(taskId);
		return this;
	}

	/**
	 * @return
	 * The state of the task
	 */
	public Integer getState() {
		return state.get();
	}

	/**
	 * Reset a task
	 * @return
	 * <tt>true</tt> if the task is resetted
	 * @throws
	 * 		LYException if the task is alive
	 */
	public boolean reset() {
		if(state.get() == STARTED) return false;
//		if(!state.compareAndSet(STOPPED, BEGAN)) return false;
		if (thread != null && getThread().isAlive())
			throw new LYException("Reset an alive task");
		thread = null;
		setObjectId(0);
		return true;
	}

	protected Thread getThread() {
		return thread;
	}

	/**
	 * Read only to outside
	 * @param thread
	 * @return
	 */
	private Task setThread(Thread thread) {
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

	public long getTimeout() {
		return timeout;
	}

	public Task setTimeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public Task setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
		return this;
	}

	public Task setController(LYTaskQueue controller) {
		this.controller = controller;
		return this;
	}

	// toString
	@Override
	public String toString() {
		String sState = "UNKNOWN";
		switch (state.get().intValue()) {
		case -3:
			sState = "STOPPED";
			break;
		case -2:
			sState = "CANCELLED";
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
		default:
			break;
		}
		return "taskId=" + getTaskId() + ",className=" + getClass().getName()
				+ ",state=" + sState + ",startTime=" + startTime + ",timeout="
				+ timeout;
	}

}
