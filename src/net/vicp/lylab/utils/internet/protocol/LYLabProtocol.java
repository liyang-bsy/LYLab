package net.vicp.lylab.utils.internet.protocol;

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

		byte[] length = Utils.IntToBytes4(data.length);
		int headLength = head.length;
		int lengthLength = length.length;
		int infoLength = info.length;
		int dataLength = data.length;
		int splitSignalLength = splitSignal.length;

		int size = headLength + lengthLength + infoLength + dataLength + splitSignalLength * 3;

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
		for (int j = 0; j < infoLength; j++)
			bytes[i++] = info[j];
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
		String sInfo = null, sData = null;
		int headEndPosition = 0, lengthEndPosition = 0, infoEndPosition = 0;
		try {
			headEndPosition = offset + head.length;

			if (bytes.length - 4 < headEndPosition + splitSignal.length)
				return null;
			int dataLength = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
			lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
			int infoLength = Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
			if (infoLength == -1)
				return null;
			infoEndPosition = lengthEndPosition + splitSignal.length + infoLength;

			sInfo = new String(bytes, lengthEndPosition + splitSignal.length, infoLength);
			sData = new String(bytes, infoEndPosition + splitSignal.length, dataLength, CoreDef.CHARSET());
			return (Confirm) Utils.deserialize(Class.forName(sInfo), sData);
		} catch (Exception e) {
			String originData = null;
			try {
				originData = new String(bytes, CoreDef.CHARSET());
			} catch (Exception ex) {
				originData = "Convert failed:" + Utils.getStringFromException(ex);
			}
			throw new LYException("Failed to convert data[" + originData + "] into specific class:" + sInfo, e);
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

		int headEndPosition = head.length + offset;

		if (bytes.length - 4 < headEndPosition + splitSignal.length)
			return 0;
		int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);
		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;

		int infoLength = Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
		if (infoLength == -1)
			return 0;
		int infoEndPosition = lengthEndPosition + splitSignal.length + infoLength;

		int dataEndPosition = length + infoEndPosition + splitSignal.length;
		if (len < dataEndPosition)
			return 0;
		return dataEndPosition;
	}

}
