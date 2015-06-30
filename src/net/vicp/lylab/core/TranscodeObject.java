package net.vicp.lylab.core;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.internet.protocol.AbstractProtocol;
import flexjson.JSONDeserializer;

/**
 * Extends TranscodeProtocol means it could be encoding into protocol.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public abstract class TranscodeObject extends BaseObject {
	public abstract AbstractProtocol encode();

	public String[] exclude(String ... excludeRule)
	{
		return new String[] { };
	}
	
	public static Object decode(AbstractProtocol protocol)
	{
		if(protocol == null)
			throw new LYException("Parameter protocol is null");
		if(protocol.getClassName() == null)
			throw new LYException("Inner className is null");
		if(protocol.getData() == null)
			throw new LYException("Inner data is null");
		try {
			return new JSONDeserializer<Object>().use(null, Class.forName(protocol.transformClassName())).deserialize(protocol.transformData());
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + protocol.transformClassName() + ". Maybe the data isn't json?", e);
		}
	}
}
