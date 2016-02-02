package net.vicp.lylab.utils.internet.protocol;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;

/**
 * A custom protocol easy transfer Objects through socket.<br>
 * Data will be transfered as byte array, encoded by java.lang.Serializable.<br>
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.19
 * @version 2.0.0
 */
public class JavaObjProtocol extends NonCloneableBaseObject implements Protocol {

	protected final byte[] head = "JavaObj".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };

	@Override
	public byte[] getHead() {
		return head;
	}

	@Override
	public byte[] encode(Confirm obj) {
		byte[] data;
		try {
			data = SerializationUtils.serialize((Serializable) obj);
		} catch (Exception e) {
			throw new LYException("Cannot serialize object into data", e);
		}

		byte[] length = Utils.int2Bytes(data.length);
		int headLength = head.length;
		int lengthLength = length.length;
		int dataLength = data.length;
		int splitSignalLength = splitSignal.length;

		int size = headLength + lengthLength + dataLength + splitSignalLength * 2;

		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < headLength; j++)
			bytes[i++] = head[j];
		for (int j = 0; j < splitSignalLength; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < lengthLength; j++)
			bytes[i++] = length[j];
		for (int j = 0; j < splitSignalLength; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < dataLength; j++)
			bytes[i++] = data[j];
		return bytes;
	}

	@Override
	public Confirm decode(byte[] bytes) {
		return decode(bytes, 0);
	}

	@Override
	public Confirm decode(byte[] bytes, int offset) {
		if (bytes == null)
			return null;
		if (!Utils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");

		int headEndPosition = offset + head.length;

		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.bytes2Int(bytes, head.length + splitSignal.length);

		byte[] data = Arrays.copyOfRange(bytes, lengthEndPosition + splitSignal.length,
				lengthEndPosition + splitSignal.length + length);
		try {
			return SerializationUtils.deserialize(data);
		} catch (Exception e) {
			throw new LYException("Failed to convert data into object", e);
		}
	}

	@Override
	public int validate(byte[] bytes, int len) {
		return validate(bytes, 0, len);
	}

	@Override
	public int validate(byte[] bytes, int offset, int len) {
		if (bytes == null)
			throw new LYException("Parameter bytes is null");
		if (len - offset < head.length)
			return 0;
		if (!Utils.checkHead(bytes, offset, len, head))
			throw new LYException("Bad data package: mismatch head");

		int endPosition = offset + head.length + splitSignal.length;
		if (len - 4 < endPosition)
			return 0;
		int length = Utils.bytes2Int(bytes, endPosition);
		endPosition += CoreDef.SIZEOF_INTEGER + splitSignal.length + length;
		
		if (len < endPosition)
			return 0;
		return endPosition;
	}

}
