package net.vicp.lylab.utils.internet.protocol;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.config.Config;

/**
 * Protocol Utils, offer a serial essential utilities function about
 * {@link net.vicp.lylab.core.interfaces.Protocol}.<br>
 * Before use it, you need offer a protocol config which register all protocol
 * you will use. <br>
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public final class ProtocolUtils extends BaseObject implements InitializeConfig {
	private static int configSize;

	private static Protocol[] rawProtocols = new Protocol[0];

	/**
	 * Pair to protocol by head with protocol config
	 * 
	 * @param head
	 * @return
	 */
	public static Protocol pairWithProtocol(byte[] head) throws Exception {
		Protocol protocol = null;
		for (Protocol rawProtocol : rawProtocols) {
			if (checkHead(head, rawProtocol.getHead())) {
				protocol = rawProtocol;
				break;
			}
		}
		if (protocol == null)
			throw new LYException("Can not pair with any protocol:\n" + new String(head).trim());
		return protocol;
	}

	/**
	 * Compare bytes start from a specific position for a specific limit
	 * 
	 * @param e1
	 *            source to compare
	 * @param e1Offset
	 *            offset from e1
	 * @param e2
	 *            destination to compare
	 * @param e2Offset
	 *            offset from e2
	 * @param cmpLength
	 *            compare for specific length
	 * @return true if e1 and e2 is the same from e1Offset and e2Offset for
	 *         comLength
	 */
	public static boolean bytesContinueWith(byte[] e1, int e1Offset, byte[] e2,
			int e2Offset, int cmpLength) {
		if (e1.length - e1Offset < e2.length - e2Offset
				|| e1.length - e1Offset < cmpLength
				|| e2.length - e2Offset < cmpLength)
			return false;
		for (int i = 0; i < cmpLength; i++)
			if (e1[i] != e2[e2Offset + i])
				return false;
		return true;
	}

	public static boolean checkHead(byte[] bytes, byte[] head) {
		if (!bytesContinueWith(bytes, 0, head, 0, head.length))
			return false;
		return true;
	}

	@Override
	public void obtainConfig(Config config) {
		setConfig(config);
	}
	
	public synchronized static void setConfig(Config config) {
		configSize = config.keySet().size();
		rawProtocols = new Protocol[configSize];
		int i = 0;
		for (String key : config.keySet()) {
			rawProtocols[i] = (Protocol) config.getInstance(key);
			i++;
		}
	}

	public static boolean isMultiProtocol() {
		return configSize != 1;
	}

}
