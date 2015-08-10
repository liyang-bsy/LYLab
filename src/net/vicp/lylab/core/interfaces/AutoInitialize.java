package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.core.exceptions.LYException;

/**
 * General Auto Initialize Object interface for all classes.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface AutoInitialize<T> {

	/**
	 * Thread-safely get its instance
	 * @param instanceClass Class of instance to be create
	 * @param constructorParameters Constructor parameters of instance to be create
	 * @return
	 * @throws LYException Reason within the exception
	 */
	public T get(Class<T> instanceClass, Object... constructorParameters) throws LYException;
	
}
