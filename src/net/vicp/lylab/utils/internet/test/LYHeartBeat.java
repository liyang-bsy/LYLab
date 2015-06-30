package net.vicp.lylab.utils.internet.test;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;

public class LYHeartBeat extends HeartBeat {
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
