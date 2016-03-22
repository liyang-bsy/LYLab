package net.vicp.lylab.utils.internet.protocol;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Protocol;

/**
 * A raw protocol works with raw dispatch handler to act proxy for client.<br>
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2016.03.21
 * @version 1.0.0
 */
public class RawProtocol extends NonCloneableBaseObject implements Protocol {

	protected final byte[] head = "".getBytes();
	protected final byte[] splitSignal = new byte[0];

	@Override
	public byte[] getHead() {
		return head;
	}

	@Deprecated
	@Override
	public byte[] encode(Confirm obj) {
		throw new LYException("Encode Confirm to bytes is not available for RawProtocol");
	}
	
	public byte[] encode(byte[] hb) {
		return hb;
	}

	@Override
	public Confirm decode(byte[] bytes) {
		throw new LYException("Decode bytes to Confirm is not available for RawProtocol");
	}

	@Override
	public Confirm decode(byte[] bytes, int offset) {
		throw new LYException("Decode bytes to Confirm is not available for RawProtocol");
	}
	
	public Object decode(byte[] bytes, int offset, int len) {
		byte[] dest = new byte[len - offset];
		System.arraycopy(bytes, offset, dest, 0, len);
		return dest;
	}

	@Override
	public int validate(byte[] bytes, int len) {
		return validate(bytes, 0, len);
	}

	@Override
	public int validate(byte[] bytes, int offset, int len) {
		return len;
	}

}
