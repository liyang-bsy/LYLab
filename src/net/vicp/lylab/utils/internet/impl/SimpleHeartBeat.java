package net.vicp.lylab.utils.internet.impl;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;

/**
 * A very simple heart beat structure.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SimpleHeartBeat extends HeartBeat {
	@Override
	public Protocol encode()
	{
		try {
			return new LYLabProtocol(this.getClass(), Utils.toJson(this, exclude()).getBytes(CoreDef.CHARSET));
		} catch (Exception e) { }
		return null;
	}
	
	@Override
	public String[] exclude(String... excludeRule) {
		return new String[] { };
	}
	
}
