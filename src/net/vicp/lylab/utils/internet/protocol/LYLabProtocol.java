package net.vicp.lylab.utils.internet.protocol;

import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;

/**
 * A custom protocol easy transfer Objects through socket.<br>
 * Data will be transfered as JSON string.<br>
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.19
 * @version 2.0.0
 */
public class LYLabProtocol extends NonCloneableBaseObject implements Protocol {

	protected final byte[] head = "LYLab".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };

	@Override
	public byte[] getHead() {
		return head;
	}

	@Override
	public byte[] encode(Confirm obj) {
		byte[] info = obj.getClass().getName().getBytes();
		byte[] data;
		try {
			data = Utils.serialize(obj).getBytes(CoreDef.CHARSET());
		} catch (Exception e) {
			throw new LYException("Cannot serialize object into data", e);
		}

		int iLength = info.length + splitSignal.length + data.length + splitSignal.length;
		byte[] length = Utils.int2Bytes(iLength);

		int size = head.length + splitSignal.length + length.length + splitSignal.length + iLength;

		byte[] bytes = new byte[size];
		int offset = 0;
		offset = Utils.writeNext(bytes, offset, head);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, length);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, info);
		offset = Utils.writeNext(bytes, offset, splitSignal);
		offset = Utils.writeNext(bytes, offset, data);
		offset = Utils.writeNext(bytes, offset, splitSignal);
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
		try {
			int dataLength = 0;
			int endPosition = offset + head.length + splitSignal.length;
			if (bytes.length - 4 < endPosition)
				return null;
			int length = Utils.bytes2Int(bytes, endPosition);
			endPosition = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER + splitSignal.length;
			if (endPosition + length > bytes.length)
				return null;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			String info = new String(bytes, endPosition, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			dataLength = Algorithm.KMPSearch(bytes, splitSignal, endPosition);
			if (dataLength == -1)
				return null;
			String data = new String(bytes, endPosition, dataLength);
			endPosition = endPosition + dataLength + splitSignal.length;

			return (Confirm) Utils.deserialize(Class.forName(info), data);
		} catch (Exception e) {
			String originData = null;
			try {
				originData = new String(bytes, CoreDef.CHARSET());
			} catch (Exception ex) {
				originData = "Encode failed:\n" + Arrays.toString(bytes) + Utils.getStringFromException(ex);
			}
			throw new LYException("Failed to convert data into object:\n" + originData, e);
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
			throw new LYException("Bad data package: mismatch head\n" + new String(bytes, offset, len - offset).trim()
					+ "\nOriginal(start from " + offset + "):\n" + new String(bytes).trim());

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
