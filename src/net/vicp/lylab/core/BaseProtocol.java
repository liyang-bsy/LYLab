package net.vicp.lylab.core;

import net.vicp.lylab.core.exception.LYException;
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
	public boolean validate(byte[] bytes, int len) {
		if (bytes == null) throw new LYException("Parameter bytes is null");
		if (!ProtocolUtils.checkHead(bytes, this.getHead()))
			throw new LYException("Bad data package: mismatch head");
		
		int headEndPosition = Algorithm.KMPSearch(bytes, getSplitSignal());
		if (headEndPosition != getHead().length)
			throw new LYException("Bad data package: end position of head not found");
		
		int lengthEndPosition = getHead().length + getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, getHead().length + getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + getSplitSignal().length + Algorithm.KMPSearch(bytes, getSplitSignal(), lengthEndPosition + getSplitSignal().length);
		if (infoEndPosition <= 0)
			throw new LYException("Bad data package: end position of info not found");

		if(len > length + infoEndPosition + getSplitSignal().length)
			throw new LYException("Bad data package: Length out of bound");
		if(len < length + infoEndPosition + getSplitSignal().length)
			return false;
		return true;
	}

}
