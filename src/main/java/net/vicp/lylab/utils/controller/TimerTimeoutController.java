package net.vicp.lylab.utils.controller;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.timer.InstantJob;

/**
 * Manager class to recycle target in watch list.<br>
 * But managed by {@link net.vicp.lylab.utils.timer.LYPlan}.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.07.01
 * @version 1.0.0
 */
public final class TimerTimeoutController extends InstantJob {
	
	private List<WeakReference<Recyclable>> watchList = new LinkedList<WeakReference<Recyclable>>();

	@Override
	public Integer getInterval() {
		return MINUTE;
	}
	
	/**
	 * Major cycle to check timoutControl()
	 */
	@Override
	public void exec() {
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
	public synchronized boolean addToWatch(Recyclable rec) {
		return watchList.add(new WeakReference<Recyclable>(rec));
	}

	/**
	 * Remove an recyclable target into watch list
	 * @param rec
	 */
	public synchronized void removeFromWatch(Recyclable rec) {
		System.gc();
		Iterator<WeakReference<Recyclable>> iterator = watchList.iterator();
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

}
