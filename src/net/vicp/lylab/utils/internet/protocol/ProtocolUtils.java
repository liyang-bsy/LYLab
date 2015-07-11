package net.vicp.lylab.utils.internet.protocol;

import java.util.Arrays;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
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
public final class ProtocolUtils extends BaseObject {
	private static Config config;

	/**
	 * Pair to protocol by head with protocol config
	 * @param head
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<Protocol> pairToProtocol(byte[] head)
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
		return protocolClass;
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

//	public static byte[] toBytes(Protocol protocol) {
//		return protocol.toBytes();
//	}

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
	
	private static boolean checkHead(Protocol protocol, byte[] bytes)
	{
		if (!bytesContinueWith(bytes, 0, protocol.getHead(), 0, protocol.getHead().length)) return false;
		return true;
	}

	/**
	 * Assemble bytes into a protocol
	 * @param bytes
	 * @return
	 */
	public static Protocol fromBytes(byte[] bytes) {
		if (bytes == null) return null;
		Class<Protocol> protocolClass = pairToProtocol(bytes);
		Protocol protocol = rawProtocol(protocolClass);
		if (protocol == null) return null;
//		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, bytes))
			return null;

		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return null;
		
		int lengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, protocol.getHead().length + protocol.getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(bytes, protocol.getSplitSignal(), lengthEndPosition + protocol.getSplitSignal().length);
		if (infoEndPosition <= 0) return null;

		byte[] info = Arrays.copyOfRange(bytes, lengthEndPosition + protocol.getSplitSignal().length, infoEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, infoEndPosition + protocol.getSplitSignal().length, infoEndPosition + protocol.getSplitSignal().length + length);

		return new AtomicStrongReference<Protocol>().get(protocolClass, info, data);
		//return new Protocol(info, data);
	}
	
	/**
	 * Assemble bytes into a protocol
	 * @param bytes
	 * @return
	 */
	public static Protocol fromBytes(Protocol protocol, byte[] bytes) {
		if (bytes == null) return null;
		if (protocol == null) {
			Class<Protocol> protocolClass = pairToProtocol(bytes);
			protocol = rawProtocol(protocolClass);
		}
//		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, bytes))
			return null;

		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return null;
		
		int lengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, protocol.getHead().length + protocol.getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(bytes, protocol.getSplitSignal(), lengthEndPosition + protocol.getSplitSignal().length);
		if (infoEndPosition <= 0) return null;

		byte[] info = Arrays.copyOfRange(bytes, lengthEndPosition + protocol.getSplitSignal().length, infoEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, infoEndPosition + protocol.getSplitSignal().length, infoEndPosition + protocol.getSplitSignal().length + length);
		protocol.setAll(info, data);
		return protocol;
		//return new Protocol(info, data);
	}

	/**
	 * Fast validate if these bytes could be assemble into a protocol
	 * @param protocol
	 * @param bytes
	 * @param len
	 * @return
	 * 0 yes, -1 no, 1 not enough
	 */
	public static int validate(Protocol protocol, byte[] bytes, int len) {
		if (protocol == null || bytes == null) return -1;
//		Protocol protocol = rawProtocol(pairToProtocol(bytes));
//		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, bytes))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return -1;
		
		int lengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, protocol.getHead().length + protocol.getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(bytes, protocol.getSplitSignal(), lengthEndPosition + protocol.getSplitSignal().length);
		if (infoEndPosition <= 0) return -1;

		if(len > length + infoEndPosition + protocol.getSplitSignal().length)
			return -1;
		if(len < length + infoEndPosition + protocol.getSplitSignal().length)
			return 1;
		return 0;
	}

	private static Config getConfig() {
		if(config == null) throw new LYException("Protocol config is null");
		return config;
	}

	public static void setConfig(Config config) {
		ProtocolUtils.config = config;
	}

	public static boolean isMultiProtocol() {
		return config.keySet().size() == 1;
	}
}
