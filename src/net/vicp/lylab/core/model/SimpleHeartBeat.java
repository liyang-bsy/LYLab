package net.vicp.lylab.core.model;

import net.vicp.lylab.core.interfaces.HeartBeat;

/**
 * A very simple heart beat structure.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SimpleHeartBeat implements HeartBeat {
	public SimpleHeartBeat() { }

	@Override
	public String toString() {
		return "This is a empty heart beat package";
	}

}
