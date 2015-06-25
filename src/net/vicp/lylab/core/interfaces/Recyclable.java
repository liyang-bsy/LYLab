package net.vicp.lylab.core.interfaces;

/**
 * 	General Recyclable interface for all classes.<br>
 * 	If run() was used.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
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
