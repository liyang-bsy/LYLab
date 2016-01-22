package net.vicp.lylab.utils.internet.protocol;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Protocol;

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
public final class ProtocolUtils extends BaseObject {
	private static Protocol[] rawProtocols = new Protocol[0];

	/**
	 * Pair to protocol by head with protocol config
	 * 
	 * @param head
	 * @return
	 */
	public static Protocol pairWithProtocol(byte[] head) {
		return pairWithProtocol(head, 0);
	}
	/**
	 * Pair to protocol by head with protocol config
	 * 
	 * @param head
	 * @return
	 */
	public static Protocol pairWithProtocol(byte[] head, int offset) {
		Protocol protocol = null;
		for (Protocol rawProtocol : rawProtocols) {
			if (checkHead(head, offset, rawProtocol.getHead())) {
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
			if (e1[e1Offset + i] != e2[e2Offset + i])
				return false;
		return true;
	}

	public static boolean checkHead(byte[] bytes, byte[] head) {
		return checkHead(bytes, 0, head);
	}

	public static boolean checkHead(byte[] bytes, int offset, byte[] head) {
		if (!bytesContinueWith(bytes, offset, head, 0, head.length))
			return false;
		return true;
	}
	
	public static boolean isMultiProtocol() {
		return rawProtocols.length > 1;
	}

	public static void setRawProtocols(Protocol... rawProtocols) {
		ProtocolUtils.rawProtocols = rawProtocols;
	}

}
