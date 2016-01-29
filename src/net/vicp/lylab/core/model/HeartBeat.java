package net.vicp.lylab.core.model;

import net.vicp.lylab.core.interfaces.Confirm;

/**
 * Heart beat for long socket, to keep alive.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public abstract class HeartBeat implements Confirm {
	@Override
	public String toString() {
		return "This is a HeartBeat package";
	}
	
}
