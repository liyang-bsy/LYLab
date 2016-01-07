package net.vicp.lylab.utils.controller;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * Manager class to recycle target in watch list.<br>
 * Will recycle if a target reports true by isRecyclable().
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2016.01.07
 * @version 2.0.1
 */
public final class TimeoutController extends LoneWolf implements LifeCycle {
	private static final long serialVersionUID = -4494667245957319328L;

	private static AutoInitialize<TimeoutController> instance = new AtomicStrongReference<TimeoutController>();

	private List<WeakReference<Recyclable>> watchList = new LinkedList<WeakReference<Recyclable>>();

	/**
	 * Initialize procedure
	 */
	@Override
	public void initialize() {
		getInstance().reset();
		getInstance().begin("TimeoutController");
	}
	
	@Override
	public void close() {
		getInstance().callStop();
	}
	
	/**
	 * TimeoutController is always a daemon
	 */
	@Override
	public boolean isDaemon() {
		return true;
	}

	/**
	 * Major cycle to check timoutControl()
	 */
	@Override
	public void exec() {
		try {
			while (!getThread().isInterrupted()) {
				if (isStopped())
					break;
				Thread.sleep(CoreDef.DEFAULT_TIMTOUT_CONTROLLER_INTERVAL);
				getInstance().timeoutControl();
			}
		} catch (InterruptedException e) {
			throw new LYException("TimeoutController is interrupted");
		}
	}

	/**
	 * Major cycle to recycle every target is recyclable
	 */
	private synchronized void timeoutControl() {
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
			} catch (Throwable t) {
				log.error(Utils.getStringFromThrowable(t));
			}
		}
	}

	/**
	 * Add an recyclable target into watch list
	 * @param rec
	 * @return
	 * <tt>true</tt>
	 */
	public synchronized static boolean addToWatch(Recyclable rec) {
		return getInstance().getWatchList().add(new WeakReference<Recyclable>(rec));
	}

	/**
	 * Remove an recyclable target into watch list
	 * @param rec
	 */
	public synchronized static void removeFromWatch(Recyclable rec) {
		System.gc();
		Iterator<WeakReference<Recyclable>> iterator = getInstance().getWatchList().iterator();
		while (iterator.hasNext()) {
			Recyclable tmp = iterator.next().get();
			if (tmp == null)
				iterator.remove();
			else if (rec.equals(tmp)) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * TimeoutController will end if interrupted
	 */
	@Override
	protected void aftermath() {
		instance = new AtomicStrongReference<TimeoutController>();
	}

	// getters below
	public static TimeoutController getInstance() {
		return instance.get(TimeoutController.class);
	}

	private List<WeakReference<Recyclable>> getWatchList() {
		return watchList;
	}

}
