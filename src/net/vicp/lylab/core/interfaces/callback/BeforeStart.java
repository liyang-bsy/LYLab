package net.vicp.lylab.core.interfaces.callback;

/**
 * Implement this means something could be done before starting.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public interface BeforeStart extends Callback {
	public void beforeStart();
}
