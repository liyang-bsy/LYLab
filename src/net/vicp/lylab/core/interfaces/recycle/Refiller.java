package net.vicp.lylab.core.interfaces.recycle;

/**
 * General Refiller interface for all classes.<br>
 * Used to refill data for 
 * {@link net.vicp.lylab.core.interfaces.recycle.Recyclable}.
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Refiller<T> {
	public T refill();
}
