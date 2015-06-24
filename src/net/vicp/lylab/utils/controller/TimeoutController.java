package net.vicp.lylab.utils.controller;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

public final class TimeoutController extends Task {
	private static final long serialVersionUID = -4494667245957319328L;

	private static Boolean init = false;
	private static TimeoutController instance = null;

	private List<Recyclable> watchList = new ArrayList<Recyclable>();
	
	@Override
	public boolean isDaemon()
	{
		return true;
	}
	
	@Override
	public void exec() {
		try {
			if (!init) {
				instance = this;
				init = true;
			}
			if (isStopped())
				return;
			while (!getThread().isInterrupted()) {
				if (isStopped())
					break;
				Thread.sleep(CoreDefine.WAITING);
				getInstance().timeoutControl();
			}
		} catch (InterruptedException e) {
			throw new LYException("TimeoutController is interrupted");
		} finally {
			instance = this;
			init = true;
		}
	}

	private void timeoutControl() {
		for(Recyclable rec : watchList)
		{
			// skip myself
			if(rec instanceof TimeoutController) continue;
			if(isStopped() && !rec.isRecyclable()) continue;
			rec.recycle();
		}
	}
	
	public static boolean addToWatch(Recyclable rec)
	{
		if(getInstance().getState() == Task.BEGAN)
			getInstance().begin();
		return getInstance().getWatchList().add(rec);
	}

	public static TimeoutController getInstance() {
		return instance;
	}

	public static void setInstance(TimeoutController instance) {
		TimeoutController.instance = instance;
	}

	private List<Recyclable> getWatchList() {
		return watchList;
	}

}
