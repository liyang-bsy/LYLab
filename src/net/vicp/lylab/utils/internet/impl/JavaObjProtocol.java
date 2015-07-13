package net.vicp.lylab.utils.internet.impl;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;

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
public class JavaObjProtocol extends BaseProtocol implements Protocol {

	protected final byte[] head = "JavaObj".getBytes();
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
		byte[] info = new byte[] { };
		byte[] data;
		try {
			data = SerializationUtils.serialize((Serializable) obj);
		} catch (Exception e) {
			throw new LYException("Cannot serialize object into data", e);
		}

		byte[] length = Utils.IntToBytes4(data.length);
		int headLength = head.length;
		int lengthLength = length.length;
		int infoLength = info.length;
		int dataLength = data.length;
		int splitSignalLength = splitSignal.length;
		
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
		if (!ProtocolUtils.checkHead(bytes, head))
			return null;

		int headEndPosition = Algorithm.KMPSearch(bytes, splitSignal);
		if (headEndPosition != head.length) return null;
		
		int lengthEndPosition = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, head.length + splitSignal.length);
		
		int infoEndPosition = lengthEndPosition + splitSignal.length + Algorithm.KMPSearch(bytes, splitSignal, lengthEndPosition + splitSignal.length);
		if (infoEndPosition <= 0) return null;

		byte[] info = Arrays.copyOfRange(bytes, lengthEndPosition + splitSignal.length, infoEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, infoEndPosition + splitSignal.length, infoEndPosition + splitSignal.length + length);
		String sInfo = new String(info);
		try {
			return SerializationUtils.deserialize(data);
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + sInfo, e);
		}
	}
	
}
