package net.vicp.lylab.utils.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.tq.Task;
import net.vicp.lylab.utils.tq.WatchDog;

public final class TimeoutController extends Task {
	private static final long serialVersionUID = -4494667245957319328L;

	private static Boolean init = false;
	private static TimeoutController instance = null;
	
	private List<WeakReference<Recyclable>> watchList = new ArrayList<WeakReference<Recyclable>>();

	public static void init() {
		if (!init) {
			instance = new TimeoutController();
			init = true;
			instance.begin("TimeoutController");
		}
	}

	@Override
	public boolean isDaemon() {
		return true;
	}

	@Override
	public void exec() {
		try {
			while (!getThread().isInterrupted()) {
				if (isStopped())
					break;
				Thread.sleep(CoreDefine.WAITING_LONG);
				getInstance().timeoutControl();
			}
		} catch (InterruptedException e) {
			throw new LYException("TimeoutController is interrupted");
		}
	}

	private void timeoutControl() {
		System.gc();
		Iterator<WeakReference<Recyclable>> iterator = watchList.iterator();
		while(iterator.hasNext())
		{
//			// skip myself
//			if(rec instanceof TimeoutController) continue;
			WeakReference<Recyclable> wref = iterator.next();
			Recyclable rec = wref.get();
			if(rec == null)
			{
				iterator.remove();
				continue;
			}
			if(!rec.isRecyclable()) continue;
			rec.recycle();
		}
	}

	public synchronized static boolean addToWatch(Recyclable rec) {
		return getInstance().getWatchList().add(new WeakReference<Recyclable>(rec));
	}

	public synchronized static void removeFromWatch(Recyclable rec) {
		getInstance().getWatchList().remove(rec);
	}

	public static TimeoutController getInstance() {
		if (instance == null)
			init();
		return instance;
	}

	private List<WeakReference<Recyclable>> getWatchList() {
		return watchList;
	}

	@Override
	protected void aftermath() {
		init = false;
		instance = null;
		super.aftermath();
	}

}
