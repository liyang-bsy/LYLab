package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.core.exception.LYException;

/**
 * 	General Auto Initialize Object interface for all classes.<br>
 * 	If run() was used.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface AutoInitialize<T> {

	public T get(Class<T> instanceClass) throws LYException;
	
}
