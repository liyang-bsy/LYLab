package net.vicp.lylab.utils.internet.impl;

import java.io.Serializable;
import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

import org.apache.commons.lang3.SerializationUtils;

/**
 * A self-defined protocol easy transfer Objects through socket.<br>
 * Data will be transfered as JSON string.<br>
 * [!] Attention, protocol object is <tt>NOT</tt> thread-safe.
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
	public byte[] encode(Object obj) {
		byte[] data;
		try {
			data = SerializationUtils.serialize((Serializable) obj);
		} catch (Exception e) {
			throw new LYException("Cannot serialize object into data", e);
		}

		byte[] length = Utils.IntToBytes4(data.length);
		int headLength = head.length;
		int lengthLength = length.length;
		int dataLength = data.length;
		int splitSignalLength = splitSignal.length;
		
		int size = headLength + lengthLength
				+ dataLength
				+ splitSignalLength * 2;
		
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
	public Object decode(byte[] bytes) {
		return decode(bytes, 0);
	}

	@Override
	public Object decode(byte[] bytes, int offset) {
		if (bytes == null) return null;
		if (!ProtocolUtils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");

		int headEndPosition = offset + head.length;
		
		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, head.length + splitSignal.length);
		
		byte[] data = Arrays.copyOfRange(bytes, lengthEndPosition + splitSignal.length, lengthEndPosition + splitSignal.length + length);
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
		if (bytes == null) throw new LYException("Parameter bytes is null");
		if (len - offset < head.length)
			return 0;
		if (!ProtocolUtils.checkHead(bytes, offset, head))
			throw new LYException("Bad data package: mismatch head");
		
		int headEndPosition = offset + head.length;
		int lengthEndPosition = headEndPosition + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int length = Utils.Bytes4ToInt(bytes, headEndPosition + splitSignal.length);

		int dataEnd = length + lengthEndPosition + splitSignal.length;
		
		if(len < dataEnd)
			return 0;
		return dataEnd;
	}
	
}
