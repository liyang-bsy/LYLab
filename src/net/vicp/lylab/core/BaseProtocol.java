package net.vicp.lylab.core;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

public abstract class BaseProtocol extends NonCloneableBaseObject implements Protocol {
	/**
	 * Fast validate if these bytes could be assemble into a protocol
	 * @param protocol
	 * @param bytes
	 * @param len
	 * @return
	 * 0 yes, -1 no, 1 not enough
	 */
	@Override
	public int validate(byte[] bytes, int len) {
		if (bytes == null) return -1;
		if (!ProtocolUtils.checkHead(this, bytes))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, getSplitSignal());
		if (headEndPosition != getHead().length) return -1;
		
		int lengthEndPosition = getHead().length + getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, getHead().length + getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + getSplitSignal().length + Algorithm.KMPSearch(bytes, getSplitSignal(), lengthEndPosition + getSplitSignal().length);
		if (infoEndPosition <= 0) return -1;

		if(len > length + infoEndPosition + getSplitSignal().length)
			return -1;
		if(len < length + infoEndPosition + getSplitSignal().length)
			return 1;
		return 0;
	}

}
