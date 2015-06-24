package net.vicp.lylab.core;

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
	public boolean isRecyclable();
	public void recycle();
}
