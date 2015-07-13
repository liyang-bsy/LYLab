package net.vicp.lylab.utils.internet.protocol;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.config.Config;

/**
 * Protocol Utils, offer a serial essential utilities function about
 * {@link net.vicp.lylab.core.interfaces.Protocol}.<br>
 * Before use it, you need offer a protocol config which register all protocol you will use.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public final class ProtocolUtils extends BaseObject implements InitializeConfig {
	private static Config config;
	private static int configSizeCache;

	/**
	 * Pair to protocol by head with protocol config
	 * @param head
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Protocol pairToProtocol(byte[] head)
	{
		String sHead = new String(head);
		Class<Protocol> protocolClass = null;
		for(String key: getConfig().keySet())
		{
			if(sHead.startsWith(key))
			{
				try {
					String info = getConfig().getString(key);
					protocolClass = (Class<Protocol>) Class.forName(info);
					break;
				} catch (Exception e) { }
			}
		}
		if(protocolClass == null)
			throw new LYException("Can not pair sHead[" + sHead + "] to any protocol");
		return rawProtocol(protocolClass);
	}
	
	/**
	 * Create a raw protocol by protocol class
	 * @param protocolClass
	 * @return
	 * null if create failed
	 */
	public static Protocol rawProtocol(Class<Protocol> protocolClass)
	{
		if(protocolClass == null)
			throw new LYException("Can not create a raw protocol with null");
		Protocol protocol = null;
		try {
			protocol = protocolClass.newInstance();
		} catch (Exception e) {
			throw new LYException("Can not create a raw protocol to validate", e);
		}
		return protocol;
	}

	/**
	 * Compare bytes start from a specific position for a specific limit
	 * @param e1 source to compare
	 * @param e1Offset offset from e1
	 * @param e2 destination to compare
	 * @param e2Offset offset from e2
	 * @param cmpLength compare for specific length
	 * @return
	 * true if e1 and e2 is the same from e1Offset and e2Offset for comLength
	 */
	public static boolean bytesContinueWith(byte[] e1, int e1Offset, byte[] e2, int e2Offset, int cmpLength)
	{
//		if(e1.length - e1Offset != e2.length - e2Offset) return false;
		if(e1.length - e1Offset < e2.length - e2Offset || e1.length - e1Offset < cmpLength || e2.length - e2Offset < cmpLength) return false;
		for(int i=0; i<cmpLength;i++)
			if(e1[i] != e2[e2Offset + i])
				return false;
		return true;
	}
	
	public static boolean checkHead(Protocol protocol, byte[] bytes)
	{
		if (!bytesContinueWith(bytes, 0, protocol.getHead(), 0, protocol.getHead().length)) return false;
		return true;
	}
	
	private static Config getConfig() {
		if(config == null) throw new LYException("Protocol config is null");
		return config;
	}

	@Override
	public void obtainConfig(Config config) {
		setConfig(config);
	}

	public static void setConfig(Config config) {
		ProtocolUtils.config = config;
		configSizeCache = config.keySet().size();
	}

	public static boolean isMultiProtocol() {
		return configSizeCache != 1;
	}
}
