package net.vicp.lylab.utils.tq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.pool.Pool;
import net.vicp.lylab.core.pool.SequencePool;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.atomic.AtomicSoftReference;

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
public final class LYTaskQueue extends Task {
	private static final long serialVersionUID = 4935143671023467585L;

	private static String permanentFileName = "lytaskqueue.bin";
	private static AutoInitialize<LYTaskQueue> instance = new AtomicSoftReference<LYTaskQueue>();

	private volatile Boolean useWatchDog = false;
	private static volatile AtomicBoolean isRunning = new AtomicBoolean(false);
	private volatile AtomicBoolean isTerminated = new AtomicBoolean(false);

	private volatile boolean recordFailed = false;
	private List<Task> forewarnList = new ArrayList<Task>();

	private volatile static Integer maxQueue = 100000;
	private volatile static Integer maxThread = 200;

	private Pool<Task> taskPool = new SequenceTemporaryPool<Task>(maxQueue);
	private Pool<Task> threadPool = new SequencePool<Task>(maxThread);

	public static final Long DEFAULT_TERMINATE_TIMEOUT = 5 * 60 * 1000L;

	// static auto initialize
	static { init(); }
	
	/**
	 * At your service!
	 * 
	 * @param task
	 *            which should be executed, it will be cloned and its clone will
	 *            be enqueued. <br>
	 *            [!] Original parameter 'task' will never be used or changed by
	 *            LYTaskQueue.addTask(Task task)
	 * @return A non-negative taskId returns to recognize specific task. <br>
	 *         -1, means LYTaskQueue didn't obtain cloned task. <br>
	 *         -2, means LYTaskQueue was try to terminate itself. No more task
	 *         will be accepted. <br>
	 *         -3, means LYTaskQueue couldn't add task into task pool.
	 */
	public static Long addTask(Task task) {
		Task task0 = (Task) task.clone();
		if (task0 == null)
			return -1L;
		if (getInstance().getIsTerminated().get())
			return -2L;
		task0.setController(getInstance());
		if (getInstance().getTaskPool().add(task0) == null)
			return -3L;
		synchronized (getInstance().lock) {
			getInstance().lock.notifyAll();
		}
		if (isRunning.compareAndSet(false, true))
			if(getInstance().reset())
				getInstance().begin("LYTaskQueue");
		return task0.getTaskId();
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	@Override
	public final void exec() {
		try {
			while (!getInstance().getIsTerminated().get()) {
				while (!getInstance().getIsTerminated().get()
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
				} catch (Throwable e) {
					log.error("Exception in LYTaskQueue#exec wait():\n"
							+ Utils.getStringFromException(e));
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
	 *            which you will get from LYTaskQueue.addTask()
	 * @return true: cancelled<br>
	 *         false: cancel failed
	 */
	public synchronized static Boolean cancel(long taskId) {
		if (getInstance().getIsTerminated().get() || taskId < 0L)
			return false;
		Task tk = getInstance().removeFromTaskPool(taskId);
		if (tk == null || tk.getState() != Task.BEGAN)
			return false;
		tk.callStop();
		return true;
	}

	/**
	 * Stop a task.
	 * 
	 * @param taskId
	 *            which you will get from LYTaskQueue.addTask()
	 * @return true: cancelled<br>
	 *         false: cancel failed
	 */
	public synchronized static Boolean stop(long taskId) {
		if (getInstance().getIsTerminated().get() || taskId < 0L)
			return false;
		Task tk = getInstance().removeFromTaskPool(taskId);
		if (tk == null)
			return false;
		tk.callStop();
		return true;
	}

	/**
	 * Initialize procedure
	 */
	public static void init() {
		getInstance().getIsTerminated().set(false);
		ObjectInputStream ois = null;
		try {
			File file = new File(permanentFileName);
			if (file.exists()) {
				ois = new ObjectInputStream(new FileInputStream(file));
				Integer total = (Integer) ois.readObject();
				while (total-- > 0) {
					Task tk = (Task) ois.readObject();
					tk.reset();
					LYTaskQueue.addTask(tk);
				}
			}
			Utils.deleteFile(permanentFileName);
		} catch (Exception e) {
			throw new LYException("Unable to load data from last permanent file", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
					ois = null;
				} catch (IOException e) {
					throw new LYException("Unable to close permanent file:" + permanentFileName, e);
				}
			}
		}
	}

	/**
	 * This action will call off the task queue, then wait tasks in running for
	 * 5 minutes, then killed, and save tasks in queue onto disk
	 */
	public static void terminate() {
		// default timeout is 5 minutes
		terminate(DEFAULT_TERMINATE_TIMEOUT);
	}

	/**
	 * This action will call off the task queue, then wait tasks in running for
	 * specific timeout, then killed, and save tasks in queue onto disk
	 * 
	 * @param timeout
	 *            waiting limit for running tasks in million second
	 */
	public static void terminate(long timeout) {
		if (getInstance().getIsTerminated().compareAndSet(false, true))
			return;
		if (timeout == 0L)
			timeout = 1L;

		synchronized (getInstance().getThreadPool()) {
			for (Task t : getInstance().getThreadPool())
				t.setTimeout(timeout);
		}

		if (!getInstance().useWatchDog)
			LYTaskQueue.useWatchDog(getInstance().useWatchDog);
		getInstance().useWatchDog = true;
		try {
			File p = new File(permanentFileName);
			if (!p.exists())
				p.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(p));
			synchronized (instance) {
				oos.writeObject(new Integer(getInstance().getTaskPool().size()));
				while (!getInstance().getTaskPool().isEmpty())
					oos.writeObject(getInstance().getTaskPool().accessOne());
			}
			oos.close();
		} catch (IOException e) {
			log.error("LYTaskQueue - safely shutdown: Permanent process error (This will cause data loss!)");
			e.printStackTrace();
		}
		try {
			while (getInstance().getThreadPool().size() > 0)
				getInstance().getThreadPool().accessOne().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
			WatchDog.killAll();
		} finally {
			WatchDog.stopWatchDog();
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
	public static void useWatchDog(Boolean useWatchDog) {
		if (useWatchDog)
			WatchDog.startWatchDog();
		else
			WatchDog.stopWatchDog();

		getInstance().useWatchDog = useWatchDog;
	}
	
	/**
	 * @return
	 * <tt>true</tt> if the thread pool is full
	 */
	public static Boolean isThreadPoolFull() {
		return getInstance().getThreadPool().size() == maxThread.intValue();
	}

	/**
	 * @return
	 * <tt>true</tt> if the task pool is full
	 */
	public static Boolean isTaskPoolFull() {
		return getInstance().getTaskPool().size() == maxQueue.intValue();
	}

	public static Integer getWaitingTaskCount() {
		return getInstance().getTaskPool().size();
	}
	
	public static Integer getRunningThreadCount() {
		return getInstance().getThreadPool().size();
	}

	// special getters & setters below
	public List<Task> getForewarnList() {
		List<Task> tmp = forewarnList;
		forewarnList = new ArrayList<Task>();
		return tmp;
	}
	
	public static void setMaxQueue(int maxQueue) {
		if(maxQueue <= 0) throw new LYException("maxQueue must be positive");
		LYTaskQueue.maxQueue = maxQueue;
		getInstance().getTaskPool().setMaxSize(LYTaskQueue.maxQueue);
	}

	public static void setMaxThread(Integer maxThread) {
		if(maxQueue <= 0) throw new LYException("maxThread must be positive");
		LYTaskQueue.maxThread = maxThread;
		getInstance().getThreadPool().setMaxSize(LYTaskQueue.maxThread);
	}
	
	public static LYTaskQueue getInstance() {
		return instance.get(LYTaskQueue.class);
	}

	public Pool<Task> getThreadPool() {
		return getInstance().threadPool;
	}
	
	private Pool<Task> getTaskPool() {
		return taskPool;
	}

	// getters & setters below
	public AtomicBoolean getIsTerminated() {
		return isTerminated;
	}

	public static Integer getMaxQueue() {
		return maxQueue;
	}

	public static Integer getMaxThread() {
		return maxThread;
	}

	public static String getPermanentFileName() {
		return permanentFileName;
	}

	public static void setPermanentFileName(String permanentFileName) {
		LYTaskQueue.permanentFileName = permanentFileName;
	}

}
