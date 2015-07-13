package net.vicp.lylab.utils.internet.impl;

import java.util.Arrays;

import net.vicp.lylab.core.BaseProtocol;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

/**
 * A self-defined protocol easy transfer Objects through socket.<br>
 * Data will be transfered as JSON string.<br>
 * [!] Attention, protocol object is <tt>NOT</tt> thread-safe.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class LYLabProtocol extends BaseProtocol implements Protocol {

	protected final byte[] head = "LYLab".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };
	
	@Override
	public byte[] getHead() {
		return head;
	}
	
	@Override
	public byte[] getSplitSignal() {
		return splitSignal;
	}
	
	@Override
	public byte[] encode(Object obj) {
		byte[] info = obj.getClass().getName().getBytes();
		byte[] data;
		try {
			data = Utils.serialize(obj).getBytes(CoreDef.CHARSET);
		} catch (Exception e) {
			throw new LYException("Cannot serialize object into data", e);
		}

		byte[] length = Utils.IntToBytes4(data.length);
		int headLength = getHead().length;
		int lengthLength = length.length;
		int infoLength = info.length;
		int dataLength = data.length;
		int splitSignalLength = getSplitSignal().length;
		
		int size = headLength + lengthLength
				+ infoLength + dataLength
				+ splitSignalLength * 3;
		
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
	public Object decode(byte[] bytes) {
		if (bytes == null) return null;
		if (!ProtocolUtils.checkHead(this, bytes))
			return null;

		int headEndPosition = Algorithm.KMPSearch(bytes, getSplitSignal());
		if (headEndPosition != getHead().length) return null;
		
		int lengthEndPosition = getHead().length + getSplitSignal().length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, getHead().length + getSplitSignal().length);
		
		int infoEndPosition = lengthEndPosition + getSplitSignal().length + Algorithm.KMPSearch(bytes, getSplitSignal(), lengthEndPosition + getSplitSignal().length);
		if (infoEndPosition <= 0) return null;

		byte[] info = Arrays.copyOfRange(bytes, lengthEndPosition + getSplitSignal().length, infoEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, infoEndPosition + getSplitSignal().length, infoEndPosition + getSplitSignal().length + length);
		String sInfo = new String(info);
		try {
			String sData = new String(data, CoreDef.CHARSET);
			return Utils.deserialize(Class.forName(sInfo), sData);
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + sInfo, e);
		}
	}
	
}
