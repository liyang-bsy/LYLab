package net.vicp.lylab.core.interfaces;


/**
 * Plugin interface, contains activity and information about this plug-in.<br>
 * But execute, or not execute this plug-in is based on its invoker.
 * 
 * @author liyang
 */
public interface Plugin extends LifeCycle {
	
	/**
	 * Plugin's priority, to estimate which plugin to execute when conflict,<br>
	 * or which is larger will be executed earlier.
	 * @return
	 */
	int priority();

	/**
	 * To indicate where this plug-in will be use.
	 * @return
	 * A binary code to show layer information
	 */
	int layer();

}
