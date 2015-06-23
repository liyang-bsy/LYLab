package net.vicp.lylab.utils.tq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.vicp.lylab.core.pool.Pool;
import net.vicp.lylab.core.pool.SequencePool;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;

/**
 * 	Manager class to execute all task.<br>
 * 	Finish tasks within certain threads.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.23
 * @version 1.1.0
 * 
 */
public final class LYTaskQueue extends Thread {

	protected static Log log = LogFactory.getLog(LYTaskQueue.class);
	
	private volatile static Boolean useWatchDog = false;
	private static String permanentFileName = "lytaskqueue.bin";

	private static Boolean isRunning = new Boolean(false);
	private static volatile boolean isTerminated = false;

	private volatile static Long lastTaskId = 0L;
	private volatile static Integer maxQueue = 1000;
	private volatile static Integer maxThread = 200;
	
	private static Pool<Task> taskPool = new SequenceTemporaryPool<Task>(maxQueue);
	private static Pool<Task> threadPool = new SequencePool<Task>(maxThread);
	
	public static final Long DEFAULT_TERMINATE_TIMEOUT = 5*60*1000L;

	/**
	 * At your service!
	 * @param
	 * task which should be executed, it will be cloned and its clone will be enqueued.
	 * <br>[!] Original parameter 'task' will never be used or changed by LYTaskQueue.addTask(Task task)
	 * @return
	 * A non-negative taskId returns to recognize specific task.
	 * <br>-1, means LYTaskQueue didn't obtain cloned task.
	 * <br>-2, means LYTaskQueue was try to terminate itself. No more task will be accepted.
	 * <br>-3, means LYTaskQueue couldn't add task into task pool.
	 */
	public synchronized static Long addTask(Task task) {
		Task task0 = null;
		try {
			task0 = (Task) task.clone();
		} catch (CloneNotSupportedException e) { }
		if (task0 == null)
			return -1L;
		if (isTerminated)
			return -2L;
		if ((lastTaskId = taskPool.add(task0)) == null)
			return -3L;
		if (!isRunning) {
			isRunning = true;
			Thread t = new LYTaskQueue();
			t.setName("LYTaskQueue");
			t.start();
		}
		return task0.getTaskId();
	}

	/**
	 * Cancel a task.
	 * @param
	 * taskId which you will get from LYTaskQueue.addTask()
	 * @return
	 * true: cancelled<br>false: cancel failed
	 */
	public synchronized static Boolean cancel(Long taskId) {
		if (isTerminated || taskId == null || taskId < 0L)
			return false;
		if (lastTaskId < taskId)
			return false;
		Task tk = taskPool.remove(taskId);
		if (tk == null || tk.getState() != Task.BEGAN)
			return false;
		tk.callStop();
		return true;
	}
	
	/**
	 * Stop a task.
	 * @param
	 * taskId which you will get from LYTaskQueue.addTask()
	 * @return
	 * true: cancelled<br>false: cancel failed
	 */
	public synchronized static Boolean stop(Long taskId) {
		if (isTerminated || taskId == null || taskId < 0L)
			return false;
		if (lastTaskId < taskId)
			return false;
		Task tk = taskPool.remove(taskId);
		if (tk == null)
			return false;
		tk.callStop();
		return true;
	}

	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	@Override
	public synchronized final void run() {
		try {
			while (!isTerminated && !taskPool.isEmpty()) {
				Task task = taskPool.accessOne();
				task.begin();
				threadPool.add(task);
			}
		} catch (Throwable e) {
		} finally {
			isRunning = false;
		}
	}
	
	public static void taskEnded(Long taskId) {
		threadPool.remove(taskId);
	}
	
	public static Boolean isUsingWatchDog() {
		return useWatchDog;
	}

	public static void useWatchDog(Boolean useWatchDog) {
		if(LYTaskQueue.useWatchDog == useWatchDog) return;
		
		if(useWatchDog)
			LYTaskQueue.addTask(new WatchDog().setTimeout(0L));
		else
			WatchDog.stopWatchDog();
		
		LYTaskQueue.useWatchDog = useWatchDog;
	}

	static {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(permanentFileName));
			Integer total = (Integer) ois.readObject();
			while (total-- > 0) {
				Task tk = (Task) ois.readObject();
				tk.reset();
				LYTaskQueue.addTask(tk);
			}
			ois.close();
			Utils.deleteFile(permanentFileName);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This action will call off the task queue, then wait tasks in running for 5 minutes, then killed, and save tasks in queue onto disk
	 */
	public static void terminate()
	{
		// default timeout is 5 minutes
		terminate(DEFAULT_TERMINATE_TIMEOUT);
	}
	
	/**
	 * This action will call off the task queue, then wait tasks in running for specific timeout, then killed, and save tasks in queue onto disk
	 * @param timeout waiting limit for running tasks in million second
	 */
	public static void terminate(Long timeout) {
		if (isTerminated)
			return;
		isTerminated = true;
		if (timeout == 0L)
			timeout = 1L;

		synchronized (getThreadPool()) {
			for (Task t : getThreadPool())
				t.setTimeout(timeout);
		}

		if (!useWatchDog)
			LYTaskQueue.useWatchDog(useWatchDog);
		LYTaskQueue.useWatchDog = true;
		try {
			File p = new File(permanentFileName);
			if (!p.exists())
				p.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(p));
			synchronized (taskPool) {
				oos.writeObject(new Integer(taskPool.size()));
				while (!taskPool.isEmpty())
					oos.writeObject(taskPool.accessOne());
			}
			oos.close();
		} catch (IOException e) {
			log.error("LYTaskQueue - safely shutdown: Permanent process error (This will cause data loss!)");
			e.printStackTrace();
		}
		try {
			while (threadPool.size() > 0)
				threadPool.accessOne().waitingForFinish();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
			WatchDog.killAll();
		} finally {
			WatchDog.stopWatchDog();
		}
	}

	public static Boolean getIsTerminated() {
		return isTerminated;
	}

	public static Pool<Task> getThreadPool() {
		return threadPool;
	}

	public static Long getLastTaskId() {
		return lastTaskId;
	}

	public static Integer getThreadCount() {
		return threadPool.size();
	}

	public static Integer getMaxQueue() {
		return maxQueue;
	}

	public static void setMaxQueue(Integer maxQueue) {
		LYTaskQueue.maxQueue = maxQueue;
	}

	public static Integer getMaxThread() {
		return maxThread;
	}

	public static void setMaxThread(Integer maxThread) {
		LYTaskQueue.maxThread = maxThread;
	}

	public static String getPermanentFileName() {
		return permanentFileName;
	}

	public static void setPermanentFileName(String permanentFileName) {
		LYTaskQueue.permanentFileName = permanentFileName;
	}

}
