package net.vicp.lylab.utils.tq;

import java.util.LinkedList;
import java.util.Queue;

import net.vicp.lylab.core.Executor;

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

	private volatile static Queue<Executor> tqs = new LinkedList<Executor>();
	private volatile static Boolean isRunning = false;
	private volatile static Integer _tc = 1;

	private static Long lastTaskId = 0L;
	private static Integer maxQueue = 1000;
	private static Integer maxThread = 200;

	/*
	 * Default is 1 second.
	 */
	private static Long waitingThreshold = 500L;
	
	/*
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
	 * 
	 */
	public static Long addTask(Task task)
	{
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
		synchronized (tqs) {
			while (true) {
				Integer size = tqs.size();
				if (size >= maxQueue) {
					try {
						tqs.wait(waitingThreshold);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (size <= maxQueue && size >= 0) {
					synchronized (lastTaskId) {
						if(size == 0) lastTaskId = 0L;
						task0.setTaskId(lastTaskId++);
							tqs.offer(task0);
					}
					break;
				}
			}
		}
		if(!isRunning)
		{
			isRunning = true;
			new LYTaskQueue().start();
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
		if(taskId == null || taskId < 0)
			return false;
		synchronized (tqs) {
			if(lastTaskId < taskId)
				return false;
			try{
				for(Executor tk: ((LinkedList<Executor>) tqs))
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
				System.out.println("tqs.size():" + tqs.size() + "\tlastTaskId:" + lastTaskId + "\ttaskId:" + taskId);
				e.printStackTrace();
			}
		}
		return false;
	}

	
	private static void execute()
	{
		while(tqs.peek() != null)
		{
			Task task = (Task) tqs.poll();
			while(task.getState() == Task.BEGAN)
			{
				synchronized (_tc) {
					if(_tc >= maxThread)
					{
						try {
							_tc.wait(waitingThreshold);
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				new Thread(task).start();
				synchronized (tqs) {
					tqs.notifyAll();
				}
				break;
			}
		}
	}
	
	public static void _inc() { synchronized(_tc) { _tc++; } }
	
	public static void _dec() { synchronized(_tc) { 
		try {
			_tc.notifyAll();
		} catch (Exception e) { }
		_tc--;
		}
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

	/**
	 * Default is 1 second.
	 */
	public static void setWaitingThreshold(Long waitingThreshold) {
		LYTaskQueue.waitingThreshold = waitingThreshold;
	}

	public static Queue<Executor> getTqs() {
		return tqs;
	}

	public static Long getLastTaskId() {
		return lastTaskId;
	}

	/**
	 * [!] Not real-time
	 * @return a number indicates running threads
	 */
	public static Integer getThreadCount() {
		return _tc;
	}
	
}
