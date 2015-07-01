package net.vicp.lylab.core.interfaces.recycle;

/**
 * General Recyclable interface for all classes.<br>
 * Implement this means it could be recycled by<br>
 * {@link net.vicp.lylab.utils.controller.TimeoutController}.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Recyclable {
	/**
	 * Check if need to recycle
	 */
	public boolean isRecyclable();
	/**
	 * Recycle it
	 */
	public void recycle();
	
}
