package net.vicp.lylab.utils.tq;

import java.io.Serializable;
import java.util.Date;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicInteger;

/**
 * Extends Task and reference to TaskQueue(manage class).<br>
 * Override exec() to satisfy your needs.
 * <br><br>
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
	protected long taskTimeout = CoreDef.DEFAULT_TASK_TTIMEOUT;
	/**
	 * How many time you want retry if this task was killed by WatchDog
	 */
	protected volatile Integer retryCount = 0;
	/**
	 * Control its running thread
	 */
	protected Thread thread = null;
	/**
	 * Report itself to LYTaskQueue
	 */
	private LYTaskQueue controller = null;

	/**
	 * Indicate when this task start run()
	 */
	protected Date startTime = null;

	private final AtomicInteger state = new AtomicInteger(BEGAN);

	static public final int STOPPED = -3;
	static public final int CANCELLED = -2;
	static public final int FAILED = -1;
	static public final int BEGAN = 0;
	static public final int STARTED = 1;
	static public final int COMPLETED = 2;

	private boolean lonewolf = false;
	@Override
	public BaseObject clone() {
		if(lonewolf)
			throw new LYException("Clone is not supported for a lone wolf");
		Task tk = (Task) super.clone();
		tk.reset();
		return tk;
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	public final void run() {
		try {
			if (!state.compareAndSet(BEGAN, STARTED))
				return;
			startTime = new Date();
			exec();
			aftermath();
		} catch (Throwable t) {
			log.error(this.toString() + "\ngot an error:\t" + Utils.getStringFromThrowable(t));
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
		while (!join(CoreDef.WAITING));
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
		if(isStopped()) return;
		Utils.printStack("CallStop follow task:" + CoreDef.LINE_SEPARATOR + this, "debug");
		synchronized (lock) {
			state.compareAndSet(STOPPED, STOPPED);
			state.compareAndSet(CANCELLED, STOPPED);
			state.compareAndSet(FAILED, STOPPED);
			state.compareAndSet(BEGAN, CANCELLED);
			state.compareAndSet(STARTED, STOPPED);
//			state.compareAndSet(COMPLETED, COMPLETED);		// non-sense
			if (getThread() != null)
				getThread().interrupt();
			if (this instanceof AutoCloseable)
				try {
					((AutoCloseable) this).close();
				} catch (Exception e) {
					log.error("Call stop AutoCloseable, but close failed" + Utils.getStringFromException(e));
				}
		}
	}

	/**
	 * If you found a task was lost itself in death loop or dead lock
	 */
	@Deprecated
	public final void forceStop() {
		if(state.compareNotAndSet(COMPLETED, STOPPED)) {
			Utils.printStack("ForceStop follow task:" + CoreDef.LINE_SEPARATOR + this, "debug");
			if (getThread() != null) {
				getThread().interrupt();
				getThread().stop(new LYException("Task " + getTaskId() + " timeout and killed"));
				setThread(null);
			}
			if(controller != null)
				controller.taskEnded(this);
		}
	}

	/**
	 * @return
	 * <tt>true</tt> if the task is still running
	 */
	public Boolean isRunning() {
		return getThread() != null && getThread().isAlive();
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
	public int getState() {
		return state.get();
	}

	/**
	 * Reset a task
	 * @return
	 * <tt>true</tt> if the task is resetted
	 * @throws
	 * 		LYException if the task is alive
	 */
	public final boolean reset() {
		if(state.get() == STARTED) return false;
//		if(!state.compareAndSet(STOPPED, BEGAN)) return false;
		if (isRunning())
			throw new LYException("Reset an alive task");
		startTime = null;
		thread = null;
		state.set(0);
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

	public long getTaskTimeout() {
		return taskTimeout;
	}

	public Task setTaskTimeout(Long taskTimeout) {
		this.taskTimeout = taskTimeout;
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

	public Task setLonewolf(boolean lonewolf) {
		this.lonewolf = lonewolf;
		// avoid WatchDog
		taskTimeout = 0L;
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
				+ ",state=" + sState + ",startTime=" + startTime + ",taskTimeout="
				+ taskTimeout;
	}

}
