package net.vicp.lylab.utils.internet.protocol;

import java.util.Arrays;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.ByteUtils;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;

/**
 * Protocol Utils, offer a serial essential utilities function about
 * {@link net.vicp.lylab.core.interfaces.Protocol}.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class ProtocolUtils extends BaseObject {

	private static AutoInitialize<Config> config = new AtomicStrongReference<Config>();
	private static String protocolConfig;

	@SuppressWarnings("unchecked")
	public static Class<Protocol> pairToProtocol(byte[] head)
	{
		String sHead = new String(head);
		Class<Protocol> protocolClass = null;
		for(String key: getProtocolConfig().keySet())
		{
			if(sHead.startsWith(key))
			{
				try {
					String className = getProtocolConfig().getString(key);
					protocolClass = (Class<Protocol>) Class.forName(className);
					break;
				} catch (Exception e) { }
			}
		}
		return protocolClass;
	}
	
	public static Protocol rawProtocol(Class<Protocol> protocolClass)
	{
		Protocol protocol = null;
		try {
			protocol = protocolClass.newInstance();
		} catch (Exception e) {
			throw new LYException("Can not create a raw protocol to validate", e);
		}
		return protocol;
	}
	
	public static Config getProtocolConfig()
	{
		if(protocolConfig == null) throw new LYException("Protocol config file path not set");
		return config.get(Config.class, protocolConfig);
	}

	public static byte[] toBytes(Protocol protocol) {
		return protocol.toBytes();
	}

	public static boolean checkHead(Protocol protocol, byte[] bytes)
	{
		if (!Arrays.equals(bytes, protocol.getHead())) return false;
		return true;
	}

	public static Protocol fromBytes(byte[] bytes) {
		if (bytes == null) return null;
		Protocol protocol = rawProtocol(pairToProtocol(bytes));
		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, temp)) return null;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return null;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + protocol.getSplitSignal().length, bytes.length);
		int dataLengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		
		int dataLength = ByteUtils.Bytes4ToInt(Arrays.copyOfRange(bytes, protocol.getHead().length + protocol.getSplitSignal().length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + protocol.getSplitSignal().length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(temp, protocol.getSplitSignal());
		if (classNameEndPosition <= 0) return null;
		byte[] className = Arrays.copyOfRange(bytes, dataLengthEndPosition + protocol.getSplitSignal().length, classNameEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, classNameEndPosition + protocol.getSplitSignal().length, classNameEndPosition + protocol.getSplitSignal().length + dataLength);

		return new AtomicStrongReference<Protocol>().get(pairToProtocol(protocol.getHead()), className, data);
		//return new Protocol(className, data);
	}

	public static int validate(byte[] bytes, int len) {
		if (bytes == null) return -1;
		Protocol protocol = rawProtocol(pairToProtocol(bytes));
		byte[] temp = Arrays.copyOfRange(bytes, 0, protocol.getHead().length);
		if (!checkHead(protocol, temp))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, protocol.getSplitSignal());
		if (headEndPosition != protocol.getHead().length) return -1;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + protocol.getSplitSignal().length, bytes.length);
		int dataLengthEndPosition = protocol.getHead().length + protocol.getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int dataLength = ByteUtils.Bytes4ToInt(Arrays.copyOfRange(bytes, protocol.getHead().length + protocol.getSplitSignal().length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + protocol.getSplitSignal().length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + protocol.getSplitSignal().length + Algorithm.KMPSearch(temp, protocol.getSplitSignal());
		if (classNameEndPosition <= 0) return -1;

		if(len > dataLength + classNameEndPosition + protocol.getSplitSignal().length)
			return -1;
		if(len < dataLength + classNameEndPosition + protocol.getSplitSignal().length)
			return 1;
		return 0;
	}
	
	public static void setProtocolConfig(String protocolConfig) {
		ProtocolUtils.protocolConfig = protocolConfig;
	}
	
}
