package net.vicp.lylab.utils.tq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.pool.Pool;
import net.vicp.lylab.core.pool.SequencePool;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * Manager class to execute all task.<br>
 * Finish tasks within certain threads.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.26
 * @version 2.0.0
 * 
 */
public final class LYTaskQueue extends LoneWolf implements LifeCycle, Recyclable {
	private static final long serialVersionUID = -406430325838413029L;

	private volatile Boolean useWatchDog = false;
	private volatile AtomicBoolean isRunning = new AtomicBoolean(false);
	private volatile AtomicBoolean closed = new AtomicBoolean(false);

	private volatile boolean recordFailed = false;
	private List<Task> forewarnList = new ArrayList<Task>();

	private volatile Integer maxQueue = 100000;
	private volatile Integer maxThread = 200;
	private volatile Long tolerance = CoreDef.WAITING_TOLERANCE;

	private Pool<Task> taskPool = new SequenceTemporaryPool<Task>(maxQueue);
	private Pool<Task> threadPool = new SequencePool<Task>(maxThread);

	/**
	 * At your service!
	 * 
	 * @param task
	 *            which should be executed, it will be cloned and its clone will
	 *            be enqueued. <br>
	 *            [!] Original parameter 'task' will never be used or changed by
	 *            addTask(Task task)
	 * @return A non-negative taskId returns to recognize specific task. <br>
	 *         -1, means this didn't obtain cloned task. <br>
	 *         -2, means this was try to terminate itself. No more task
	 *         will be accepted. <br>
	 *         -3, means this couldn't add task into task pool.
	 */
	public Long addTask(Task task) {
		Task task0 = (Task) task.clone();
		if (task0 == null)
			return -1L;
		if (closed.get())
			return -2L;
		task0.setController(this);
		if (taskPool.add(task0) == null)
			return -3L;
		synchronized (lock) {
			lock.notifyAll();
		}
		if (isRunning.compareAndSet(false, true))
			if(reset())
				begin("LYTaskQueue");
		return task0.getTaskId();
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	@Override
	public final void exec() {
		try {
			while (!closed.get()) {
				while (!closed.get()
						&& !getTaskPool().isEmpty()
						&& !isThreadPoolFull()) {
					Task task = getTaskPool().accessOne();
					synchronized (lock) {
						task.begin();
						threadPool.add(task);
					}
				}
				try {
					synchronized (lock) {
						lock.wait(CoreDef.WAITING_SHORT);
					}
				} catch (Throwable t) {
					log.error("Exception in this#exec wait():\n"
							+ Utils.getStringFromThrowable(t));
				}
			}
		} catch (Throwable e) {
		} finally {
			isRunning.set(false);
		}
	}

	/**
	 * Cancel a task.
	 * 
	 * @param taskId
	 *            which you will get from addTask()
	 * @return true: cancelled<br>
	 *         false: cancel failed
	 */
	public synchronized Boolean cancel(long taskId) {
		if (closed.get() || taskId < 0L)
			return false;
		Task tk = removeFromTaskPool(taskId);
		if (tk == null || tk.getState() != Task.BEGAN)
			return false;
		tk.callStop();
		return true;
	}

	/**
	 * Stop a task.
	 * 
	 * @param taskId
	 *            which you will get from addTask()
	 * @return true: cancelled<br>
	 *         false: cancel failed
	 */
	public synchronized Boolean stop(long taskId) {
		if (closed.get() || taskId < 0L)
			return false;
		Task tk = removeFromTaskPool(taskId);
		if (tk == null)
			return false;
		tk.callStop();
		return true;
	}

	/**
	 * Initialize procedure
	 */
	@Override
	public void initialize() {
		closed.set(false);
		if(CoreDef.config.containsKey("permanentFileName"))
		{
			File file = new File(CoreDef.config.getString("permanentFileName"));
			if (file.exists()) {
				try (ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(file));) {
					Integer total = (Integer) ois.readObject();
					while (total-- > 0) {
						Task tk = (Task) ois.readObject();
						tk.reset();
						addTask(tk);
					}
					ois.close();
					Utils.deleteFile(CoreDef.config.getString("permanentFileName"));
				} catch (Exception e) {
					throw new LYException(
							"Unable to load data from last permanent file", e);
				}
			}
		}
	}

	/**
	 * This action will call off the task queue, then wait tasks in running for
	 * 5 minutes, then killed, and save tasks in queue onto disk
	 */
	@Override
	public void close() {
		// default timeout is 5 minutes
		close(CoreDef.DEFAULT_TERMINATE_TIMEOUT);
	}

	/**
	 * This action will call off the task queue, then wait tasks in running for
	 * specific timeout, then killed, and save tasks in queue onto disk
	 * 
	 * @param timeout
	 *            waiting limit for running tasks in million second
	 */
	public void close(long timeout) {
		if (!closed.compareAndSet(false, true))
			return;
		if (timeout == 0L)
			timeout = 1L;

		synchronized (threadPool) {
			for (Task t : threadPool)
				t.setTimeout(timeout);
		}

		if (!useWatchDog)
			useWatchDog(useWatchDog);
		useWatchDog = true;
		try {
			while (threadPool.size() > 0)
				threadPool.accessOne().join();
		} catch (Throwable e) {
			killAll();
		} finally {
			stopWatchDog();
		}
		if (!getTaskPool().isEmpty()) {
			if(CoreDef.config.containsKey("permanentFileName"))
			{
				File p = new File(CoreDef.config.getString("permanentFileName"));
				if (!p.exists()) {
					try {
						p.createNewFile();
					} catch (IOException e) {
						log.error("LYTaskQueue - safely shutdown: Permanent process error (This may cause data loss!), reason:"
								+ Utils.getStringFromException(e));
					}
				}
				if (p.exists()) {
					try (ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(p));) {
						oos.writeObject(new Integer(taskPool.size()));
						while (!taskPool.isEmpty())
							oos.writeObject(taskPool.accessOne());
					} catch (IOException e) {
						log.error("LYTaskQueue - safely shutdown: Permanent process error (This will cause data loss!), reason:"
								+ Utils.getStringFromException(e));
					}
				}
			}
		}
	}

	// Functional methods
	/**
	 * Remove specific task out of thread pool, please call after you ensure the task is ended.
	 * <br>By the way, it will be called if any task is end.
	 */
	public void taskEnded(Task task) {
		if(task == null) return;
		if(task.getTaskId() != null)
		{
			Task tmp = removeFromThreadPool(task.getTaskId());
			if(recordFailed && tmp != null && task.getState() != Task.COMPLETED)
				forewarnList.add(tmp);
		}
	}
	/**
	 * Remove specific task out of thread pool, but can't determine if this task is alive
	 * @param taskId
	 * @return the removed task, {@code null} if the specific task doesn't not exist
	 */
	private Task removeFromThreadPool(long taskId)
	{
		Task tmp = null;
		synchronized (lock) {
			tmp = getThreadPool().remove(taskId);
			lock.notifyAll();
		}
		return tmp;
	}

	/**
	 * Remove specific task out of task pool
	 * @param taskId
	 * @return the removed task, {@code null} if the specific task doesn't not exist
	 */
	private Task removeFromTaskPool(long taskId)
	{
		return getTaskPool().remove(taskId);
	}

	/**
	 * Turn on WatchDog
	 */
	public void useWatchDog(Boolean useWatchDog) {
		if (useWatchDog)
			startWatchDog();
		else
			stopWatchDog();

		this.useWatchDog = useWatchDog;
	}
	
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
			Iterator<Task> iterator = getThreadPool().iterator();
			try {
				while (iterator.hasNext()) {
					Task task = iterator.next();
					// -1L means infinite
					if (task.getTimeout() == -1L)
						continue;
					if(task.getStartTime() == null) {
						log.error("A running task missing its start time will be killed immediately:" + task.toString());
						forceStopList.add(task);
					}
					if (task.getTimeout() + tolerance < new Date().getTime() - task.getStartTime().getTime())
						forceStopList.add(task);
					else if (task.getTimeout() < new Date().getTime() - task.getStartTime().getTime())
						callStopList.add(task);
				}
				finished = true;
			} catch (NullPointerException | ConcurrentModificationException e) { }
		} while (!finished);
		for(Task task : forceStopList)
		{
			task.forceStop();
			if(task.getRetryCount() > 0)
			{
				log.error("Timeout task was killed, but this task requested retry(" + task.getRetryCount() + "):\n" + task.toString());
				task.setRetryCount(task.getRetryCount() - 1);
				task.reset();
				addTask(task);
			}
		}
		for(Task task : callStopList)
		{
			task.callStop();
			log.info("Try to stop timeout task:" + task.getTaskId());
		}
	}

	/**
	 * Call off WatchDog
	 */
	protected void stopWatchDog() {
		TimeoutController.removeFromWatch(this);
	}

	/**
	 * Turn on WatchDog
	 */
	protected void startWatchDog() {
		TimeoutController.addToWatch(this);
	}

	/**
	 * You should better keep away from this, kill all tasks is really dangerous
	 */
	@SuppressWarnings("deprecation")
	public void killAll() {
		for(Task task : getThreadPool())
			task.forceStop();
	}

	/**
	 * @return
	 * <tt>true</tt> if the thread pool is full
	 */
	public Boolean isThreadPoolFull() {
		return getThreadPool().size() == maxThread.intValue();
	}

	/**
	 * @return
	 * <tt>true</tt> if the task pool is full
	 */
	public Boolean isTaskPoolFull() {
		return taskPool.size() == maxQueue.intValue();
	}

	public Integer getWaitingTaskCount() {
		return taskPool.size();
	}
	
	public Integer getRunningThreadCount() {
		return getThreadPool().size();
	}

	// special getters & setters below
	public List<Task> getForewarnList() {
		List<Task> tmp = forewarnList;
		forewarnList = new ArrayList<Task>();
		return tmp;
	}
	
	public void setMaxQueue(int maxQueue) {
		if(maxQueue <= 0) throw new LYException("maxQueue must be positive");
		this.maxQueue = maxQueue;
		taskPool.setMaxSize(maxQueue);
	}

	public void setMaxThread(int maxThread) {
		if(maxThread <= 0) throw new LYException("maxThread must be positive");
		this.maxThread = maxThread;
		getThreadPool().setMaxSize(maxThread);
	}
	
	public Pool<Task> getThreadPool() {
		return threadPool;
	}
	
	private Pool<Task> getTaskPool() {
		return taskPool;
	}

	// getters & setters below
	public boolean isClosed() {
		return closed.get();
	}

	public Integer getMaxQueue() {
		return maxQueue;
	}

	public Integer getMaxThread() {
		return maxThread;
	}

	public Long getTolerance() {
		return tolerance;
	}

	public void setTolerance(Long tolerance) {
		this.tolerance = tolerance;
	}

}
