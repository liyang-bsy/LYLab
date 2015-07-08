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
					String className = getConfig().getString(key);
					protocolClass = (Class<Protocol>) Class.forName(className);
					break;
				} catch (Exception e) { }
			}
		}
		if(protocolClass == null)
			throw new LYException("Can not pair sHead[" + sHead + "] to any protocol");
		return protocolClass;
	}
	
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

	public static byte[] toBytes(Protocol protocol) {
		return protocol.toBytes();
	}

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

	public static Protocol fromBytes(byte[] bytes) {
		if (bytes == null) return null;
		Protocol protocol = rawProtocol(pairToProtocol(bytes));
		if (protocol == null) return null;
		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, temp)) return null;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return null;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + protocol.getSplitSignal().length, bytes.length);
		int dataLengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		
		int dataLength = Utils.Bytes4ToInt(Arrays.copyOfRange(bytes, protocol.getHead().length + protocol.getSplitSignal().length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + protocol.getSplitSignal().length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(temp, protocol.getSplitSignal());
		if (classNameEndPosition <= 0) return null;
		byte[] className = Arrays.copyOfRange(bytes, dataLengthEndPosition + protocol.getSplitSignal().length, classNameEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, classNameEndPosition + protocol.getSplitSignal().length, classNameEndPosition + protocol.getSplitSignal().length + dataLength);

		return new AtomicStrongReference<Protocol>().get(pairToProtocol(protocol.getHead()), className, data);
		//return new Protocol(className, data);
	}

	public static int validate(Protocol protocol, byte[] bytes, int len) {
		if (protocol == null || bytes == null) return -1;
//		Protocol protocol = rawProtocol(pairToProtocol(bytes));
//		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, bytes))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return -1;
		
		int dataLengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int dataLength = Utils.Bytes4ToInt(bytes, protocol.getHead().length + protocol.getSplitSignal().length);
		
		int classNameEndPosition = dataLengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(bytes, protocol.getSplitSignal(), dataLengthEndPosition + protocol.getSplitSignal().length);
		if (classNameEndPosition <= 0) return -1;

		if(len > dataLength + classNameEndPosition + protocol.getSplitSignal().length)
			return -1;
		if(len < dataLength + classNameEndPosition + protocol.getSplitSignal().length)
			return 1;
		return 0;
	}

	public static Config getConfig() {
		if(config == null) throw new LYException("Protocol config is null");
		return config;
	}

	public static void setConfig(Config config) {
		ProtocolUtils.config = config;
	}
	
}
