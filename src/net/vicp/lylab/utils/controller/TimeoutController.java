package net.vicp.lylab.utils.controller;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.vicp.lylab.core.CoreDefine;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

public final class TimeoutController extends Task {
	private static final long serialVersionUID = -4494667245957319328L;

	protected Log log = LogFactory.getLog(getClass());
	
	private static Boolean init = false;
	private static TimeoutController instance = null;

	private List<WeakReference<Recyclable>> watchList = new LinkedList<WeakReference<Recyclable>>();

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
		while (iterator.hasNext()) {
			Recyclable rec = iterator.next().get();
			if (rec == null) {
				iterator.remove();
				continue;
			}
			try {
				if (rec.isRecyclable()) rec.recycle();
			} catch (Throwable e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public synchronized static boolean addToWatch(Recyclable rec) {
		return getInstance().getWatchList().add(new WeakReference<Recyclable>(rec));
	}

	public synchronized static void removeFromWatch(Recyclable rec) {
		System.gc();
		Iterator<WeakReference<Recyclable>> iterator = getInstance().getWatchList().iterator();
		while (iterator.hasNext()) {
			Recyclable tmp = iterator.next().get();
			if (tmp == null)
				iterator.remove();
			else if (rec.equals(tmp))
			{
				iterator.remove();
				break;
			}
		}
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
