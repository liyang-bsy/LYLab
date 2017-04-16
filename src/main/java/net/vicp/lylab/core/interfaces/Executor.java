package net.vicp.lylab.core.interfaces;

/**
 * General Executor interface for all classes, means to execute something.<br>
 * If run() was used. The class must define a method of no arguments called exec.<br>
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Executor {
	public void exec();
}
