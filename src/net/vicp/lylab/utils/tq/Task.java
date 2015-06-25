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
 * @since 2015.03.17
 * @version 1.0.1
 * 
 */
public abstract class Task extends CloneableBaseObject implements Runnable,
		Executor, Serializable {

	private static final long serialVersionUID = -505125638835928043L;
	/**
	 * This value doesn't make any sense if you didn't use WatchDog
	 */
	protected Long timeout;
	/**
	 * How many time you want retry if this task was killed by WatchDog
	 */
	protected volatile Integer retryCount = 0;
	/**
	 * Control its running thread
	 */
	protected Thread thread;

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
		timeout = CoreDefine.DEFAUL_TTIMEOUT;
		state.set(BEGAN);
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	public final void run() {
		try {
			int past = state.getAndSet(STARTED);
			if(past != BEGAN)
			{
				state.set(past);
				return;
			}
			setStartTime(new Date());
			try {
				exec();
			} catch (Throwable e) {
				System.err.print(this.toString() + "\ncreated an error:\t" + Utils.getStringFromException(e));
				if (state.get().intValue() == STARTED)
					state.set(FAILED);
			} finally {
				if (state.get().intValue() == STARTED)
					state.set(COMPLETED);
			}

			synchronized (this) {
				this.notifyAll();
			}
			aftermath();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			LYTaskQueue.taskEnded(getTaskId());
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
	public synchronized final void waitingForFinish() throws InterruptedException {
		while (!waitingForFinish(CoreDefine.WAITING));
	}

	/**
	 * Alert! This function will block current thread! The task is finished when
	 * this function is completed. DO NOT use it with aftermath()
	 * 
	 * @throws InterruptedException
	 */
	public synchronized final boolean waitingForFinish(Long millionseconds) throws InterruptedException {
		if (state.get().intValue() == STOPPED || state.get().intValue() == BEGAN || state.get().intValue() == STARTED)
			this.wait(millionseconds);
		if (state.get().intValue() == STOPPED || state.get().intValue() == BEGAN || state.get().intValue() == STARTED)
			return false;
		return true;
	}

	public final void begin() {
		begin(null);
	}

	public final synchronized void begin(String threadName) {
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

	public final void callStop() {
		switch (state.get().intValue()) {
		case STOPPED:
			state.set(STOPPED);
			break;
		case CANCELLED:
			state.set(STOPPED);
			break;
		case FAILED:
			state.set(STOPPED);
			break;
		case BEGAN:
			state.set(CANCELLED);
			break;
		case STARTED:
			state.set(STOPPED);
			break;
		case COMPLETED:
			break;
		default:
			throw new LYException("Unknow state code: " + state);
		}
		if (thread != null)
			thread.interrupt();
	}

	@Deprecated
	public final synchronized void forceStop() {
		synchronized (this) {
			this.notifyAll();
		}
		if (thread != null) {
			getThread().stop(new LYException("Task " + getTaskId() + " timeout"));
			thread = null;
		}
		LYTaskQueue.taskEnded(getTaskId());
	}

	public Boolean isStopped() {
		return getState() == STOPPED || getState() == CANCELLED || getState() == FAILED;
	}

	protected boolean isDaemon() {
		return false;
	}

	public final Long getTaskId() {
		return getObjectId();
	}

	public final Task setTaskId(Long taskId) {
		this.setObjectId(taskId);
		return this;
	}

	public Integer getState() {
		return state.get().intValue();
	}

	@SuppressWarnings("deprecation")
	public Task reset() {
		setObjectId(null);
		state.set(BEGAN);
		if (thread != null && getThread().isAlive())
			getThread().stop();
		thread = null;
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

	public Integer getRetryCount() {
		return retryCount;
	}

	public Task setRetryCount(Integer retryCount) {
		synchronized (retryCount) {
			this.retryCount = retryCount;
		}
		return this;
	}

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
