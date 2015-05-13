package net.vicp.lylab.utils.tq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.vicp.lylab.core.Executor;
import net.vicp.lylab.utils.Utils;

/**
 * 	Manager class to execute all task.<br>
 * 	Finish tasks within certain threads.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.1
 * 
 */
public final class LYTaskQueue extends Thread implements Runnable{

	protected static Log log = LogFactory.getLog(LYTaskQueue.class);
	
	private volatile static Queue<Executor> taskQueue = new LinkedList<Executor>();
	private volatile static List<Task> threadPool = new ArrayList<Task>();
	private volatile static Boolean isRunning = false;
	private volatile static Boolean useWatchDog = false;
	private static String permanentFileName = "/Users/liyang/Desktop/lytaskqueue.bin";

	private volatile static Boolean terminated = false;

	private volatile static Long lastTaskId = 0L;
	private volatile static Integer maxQueue = 1000;
	private volatile static Integer maxThread = 2;

	private static Long waitingThreshold = 1000L;
	
	/**
	 * Reserved entrance for multi-threaded. DO NOT call this method.
	 */
	@Override
	public final void run() {
		try {
			execute();
		} catch (Throwable e) {
		} finally {
			isRunning = false;
		}
	}

	/**
	 * At your service!
	 * @param
	 * task which should be executed, it will be cloned and its clone will be enqueued.
	 * <br>[!] Original parameter 'task' will never be used or changed by LYTaskQueue.addTask(Task task)
	 * @return
	 * A taskId used to recognise specific task.
	 * <br>If returns '-1', means LYTaskQueue was try to terminate itself. No more task will be accepted.
	 */
	public static Long addTask(Task task)
	{
		if(terminated)
			return -1L;
		if(task == null)
			return null;
		Task task0 = null;
		try {
			task0 = (Task) task.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		if(task0 == null)
			return null;
		while (true) {
			Integer size = taskQueue.size();
			if (size >= maxQueue) {
				try {
					taskQueue.wait(waitingThreshold);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (size <= maxQueue && size >= 0) {
				synchronized (lastTaskId) {
					if(lastTaskId == Long.MAX_VALUE) lastTaskId = 0L;
					task0.setTaskId(lastTaskId++);
					while(!taskQueue.offer(task0));
				}
				break;
			}
		}
		synchronized (isRunning) {
			if(!terminated && !isRunning)
			{
				isRunning = true;
				new LYTaskQueue().start();
			}
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
	public static Boolean cancel(Long taskId) {
		if(terminated || taskId == null || taskId < 0)
			return false;
		synchronized (taskQueue) {
			if(lastTaskId < taskId)
				return false;
			try{
				for(Executor tk: ((LinkedList<Executor>) taskQueue))
					if(taskId.equals(((Task) tk).getTaskId()))
					{
						synchronized (((Task) tk).getState()) {
							if (((Task) tk).getState() != Task.BEGAN)
								return false;
							((Task) tk).setState(Task.CANCELLED);
							return true;
						}
					}
			} catch(Exception e) {
				System.out.println("taskQueue.size():" + taskQueue.size() + "\tlastTaskId:" + lastTaskId + "\ttaskId:" + taskId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private synchronized static void execute()
	{
		while(!terminated && taskQueue.peek() != null)
		{
			Task task = (Task) taskQueue.peek();
			if(!task.getState().equals(Task.BEGAN))
				taskQueue.poll();
			else
			{
				synchronized (threadPool) {
					if(threadPool.size() >= maxThread)
					{
						try {
							threadPool.wait(waitingThreshold);
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					synchronized (taskQueue)
					{
						// keep original taskQueue
						if(terminated) break;
						if(taskQueue.poll() != null)
						{
							task.setStartTime(new Date());
							Thread t = new Thread(task);
							task.setThread(t);
							t.start();
							threadPool.add(task);
						}
						taskQueue.notifyAll();
					}
				}
				break;
			}
		}
	}
	
	public static void taskEnded(Long taskId) {
		synchronized(threadPool) { 
			try {
				threadPool.notifyAll();
			} catch (Exception e) { }
			for(int i=0;i<threadPool.size();i++)
			{
				if(threadPool.get(i).getTaskId().equals(taskId))
				{
					threadPool.remove(i);
					break;
				}
			}
		}
	}
	
	public static Boolean isUsingWatchDog() {
		return useWatchDog;
	}

	public static void useWatchDog(Boolean useWatchDog) {
		if(LYTaskQueue.useWatchDog == useWatchDog) return;
		
		if(useWatchDog)
		{
			WatchDog wd = (WatchDog) new WatchDog()
				.setStartTime(new Date())
				.setTimeout(0L);
			Thread t = new Thread(wd);
			wd.setThread(t);
			t.start();
		}
		else
			WatchDog.stopWatchDog();
		
		LYTaskQueue.useWatchDog = useWatchDog;
	}
	
	static {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(permanentFileName));
			Integer total = (Integer) ois.readObject();
			while(total-->0)
			{
				Task tk = (Task) ois.readObject();
				tk.setState(Task.BEGAN);
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
	
	public static void terminate()
	{
		// default timeout is 5 minutes
		terminate(5*60*1000L);
	}
	
	public static void terminate(Long timeout) {
		if(LYTaskQueue.terminated == true) return;
		LYTaskQueue.terminated = true;
		if(timeout == 0L) timeout = 1L;

		synchronized (getThreadPool()) {
			for(Task t:getThreadPool())
				t.setTimeout(timeout);
		}
		
		if(!useWatchDog)
			LYTaskQueue.useWatchDog(useWatchDog);
		LYTaskQueue.useWatchDog = true;
		try {
			File p = new File(permanentFileName);
			if(!p.exists()) p.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(p));
			synchronized (taskQueue) {
				oos.writeObject(new Integer(taskQueue.size()));
				while(!taskQueue.isEmpty())
					oos.writeObject(taskQueue.poll());
			}
			oos.close();
		} catch (IOException e) {
			log.error("LYTaskQueue - safely shutdown: Permanent process error (This will cause data loss!)");
			e.printStackTrace();
		}
		while(threadPool.size() > 0)
			threadPool.get(0).waitingForFinish();
		WatchDog.stopWatchDog();
	}

	public static Boolean getTerminated() {
		return terminated;
	}

	public static Queue<Executor> getTaskQueue() {
		return taskQueue;
	}

	public static List<Task> getThreadPool() {
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

	public static Long getWaitingThreshold() {
		return waitingThreshold;
	}

	public static void setWaitingThreshold(Long waitingThreshold) {
		LYTaskQueue.waitingThreshold = waitingThreshold;
	}

}
