package net.vicp.lylab.utils.tq;

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

	private Long taskId;
	private Integer state = BEGAN;

	static public final Integer FAILED = -1;
	static public final Integer BEGAN = 0;
	static public final Integer STARTED = 1;
	static public final Integer COMPLETED = 2;
	static public final Integer CANCELLED = 3;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	public final void run()
	{
		synchronized (getState()) {
			if(getState() != BEGAN)
				return;
			setState(STARTED);
		}
		LYTaskQueue._inc();
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		LYTaskQueue._dec();
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
			while(getState() == BEGAN || getState() == STARTED)
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

}
