package net.vicp.lylab.utils.tq;

import java.io.Serializable;
import java.util.Date;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.Utils;

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
public abstract class Task extends CloneableBaseObject implements Runnable, Executor, Recyclable, Serializable {

	private static final long serialVersionUID = -505125638835928043L;
	/**
	 * This value doesn't make any sense if you didn't use WatchDog
	 */
	protected Long timeout;
	/**
	 * How many time you want retry if this task was killed by WatchDog
	 */
	protected volatile Integer retryCount = 0;
	protected Thread thread;
	
	protected volatile Integer state = new Integer(0);
	protected Date startTime;
	
	static public Long DEFAULTTIMEOUT = 60*60*1000L;			// 1 hour
	
	static public final int STOPPED = -3;
	static public final int CANCELLED = -2;
	static public final int FAILED = -1;
	static public final int BEGAN = 0;
	static public final int STARTED = 1;
	static public final int COMPLETED = 2;

	public Task()
	{
		thread = null;
		startTime = null;
		timeout = DEFAULTTIMEOUT;
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
		try {
			state = STARTED;
			setStartTime(new Date());
			try {
				exec();
			} catch (Throwable e) {
				System.err.print(this.toString() +"\ncreated an error:\t" + Utils.getStringFromException(e));
				if (state.intValue() == STARTED)
					state = FAILED;
			} finally {
				if (state.intValue() == STARTED)
					state = COMPLETED;
			}

			synchronized (this) {
				this.notifyAll();
			}
			aftermath();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			LYTaskQueue.taskEnded(getTaskId());
		}
	}
	
	/**
	 * If you need do something when this task completed, override this.<br>
	 * It will execute unless this task was successfully CANCELLED<br><br>
	 */
	public void aftermath()
	{
		return;
	}

	/**
	 * Alert! This function will block current thread!
	 * The task is finished when this function is completed.
	 * DO NOT use it with aftermath()
	 * @throws InterruptedException 
	 */
	public synchronized final void waitingForFinish() throws InterruptedException {
		while(!waitingForFinish(CoreDefine.WAITING));
	}
	/**
	 * Alert! This function will block current thread!
	 * The task is finished when this function is completed.
	 * DO NOT use it with aftermath()
	 * @throws InterruptedException 
	 */
	public synchronized final boolean waitingForFinish(Long millionseconds) throws InterruptedException {
		if(state.intValue() == STOPPED || state.intValue() == BEGAN || state.intValue() == STARTED)
				this.wait(millionseconds);
		if(state.intValue() == STOPPED || state.intValue() == BEGAN || state.intValue() == STARTED)
			return false;
		return true;
	}
	
	public final synchronized void begin() {
		if(state.intValue() != BEGAN)
			return;
		Thread t = new Thread(this);
		t.setName("Task(" + String.valueOf(getTaskId()) + ") - " + this.getClass().getSimpleName() + "");
		t.setDaemon(isDaemon());
		this.setThread(t);
		t.start();
	}
	
	public final void callStop() {
		switch (state.intValue()) {
		case STOPPED:
			state = STOPPED;
			break;
		case CANCELLED:
			state = STOPPED;
			break;
		case FAILED:
			state = STOPPED;
			break;
		case BEGAN:
			state = CANCELLED;
			break;
		case STARTED:
			state = STOPPED;
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
	@Override
	public final synchronized void forceStop() {
		synchronized (this) {
			this.notifyAll();
		}
		LYTaskQueue.taskEnded(getTaskId());
		recycle();
	}

	public Boolean isStopped() {
		return getState() == STOPPED || getState() == CANCELLED || getState() == FAILED;
	}
	
	@Override
	public boolean isRecycled() {
		return getRetryCount() > 0 || getState() == FAILED;
	}
	
	@Override
	public boolean isRecyclable() {
		return getState() == STOPPED || getState() == CANCELLED;
	}

	@Override
	public boolean recycle() {
		setRetryCount(getRetryCount() - 1);
		reset();
		if(getRetryCount() > 0)
			LYTaskQueue.addTask(this);
		return true;
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
		return state.intValue();
	}
	
	@SuppressWarnings("deprecation")
	public Task reset() {
		setObjectId(null);
		state = BEGAN;
		if(thread != null && getThread().isAlive())
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
	public String toString()
	{
		String sState = "UNKNOWN";
		switch (state.intValue()) {
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
		return "taskId=" + getTaskId() + ",className=" + getClass().getName() + ",state=" + sState + ",startTime=" + startTime + ",timeout=" + timeout;
	}

}
