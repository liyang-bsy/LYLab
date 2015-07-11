package net.vicp.lylab.utils.internet.impl;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;

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
public class LYLabProtocol extends CloneableBaseObject implements Protocol {

	protected final byte[] head = "LYLab".getBytes();
	protected final byte[] splitSignal = new byte[] { -15 };
	
	protected byte[] length;
	protected byte[] info;
	protected byte[] data;
	
	@Override
	public byte[] getHead() {
		return head;
	}
	
	@Override
	public byte[] getSplitSignal() {
		return splitSignal;
	}
	
	/**
	 * Create a raw {@link net.vicp.lylab.core.interfaces.Protocol} object
	 */
	public LYLabProtocol() {
//		this(new byte[] { }, new byte[] { });
	}
	
	public LYLabProtocol(Protocol protocol) {
		this(protocol.getInfo(), protocol.getData());
	}
	
	public LYLabProtocol(Class<?> clazz, byte[] data) {
		this(clazz.getName().getBytes(), data);
	}
	
	public LYLabProtocol(byte[] info, byte[] data) {
		this.info = info;
		this.data = data;
		this.length = Utils.IntToBytes4(data.length);
	}

	@Override
	public byte[] encode(Object obj) {
		try {
			return this.setAll(obj.getClass().getName().getBytes(), Utils.serialize(obj).getBytes(CoreDef.CHARSET)).toBytes();
		} catch (Exception e) { throw new LYException("Encode failed", e); }
	}
	
	@Override
	public void encode(Protocol protocol, Object obj) {
		try {
			protocol.setAll(obj.getClass().getName().getBytes(), Utils.serialize(obj).getBytes(CoreDef.CHARSET));
		} catch (Exception e) { throw new LYException("Encode failed", e); }
	}

	@Override
	public Object decode() {
		if (getInfo() == null)
			throw new LYException("Inner info is null");
		if (getData() == null)
			throw new LYException("Inner data is null");
		try {
			return Utils.deserialize(Class.forName(new String(getInfo())),
					new String(getData(), CoreDef.CHARSET));
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:"
					+ new String(getInfo()) + ". Maybe the data isn't json?", e);
		}
	}
	
	@Override
	public Protocol setAll(byte[] info, byte[] data) {
		this.info = info;
		this.data = data;
		this.length = Utils.IntToBytes4(data.length);
		return this;
	}

	@Override
	public byte[] toBytes() {
		int headLength = getHead().length;
		int lengthLength = getLength().length;
		int infoLength = getInfo().length;
		int dataLength = getData().length;
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
	public String toString() {
		try {
			String description = "{info=" + new String(getInfo()) + ",length=" + Utils.Bytes4ToInt(length)
					+ ",data=" + new String(getData(), CoreDef.CHARSET) + "}";
			return description;
		} catch (Exception e) {
			throw new LYException("Can not encode data into " + CoreDef.CHARSET + " string");
		}
	}

	@Override
	public byte[] getLength() {
		return length;
	}

	@Override
	public byte[] getInfo() {
		return info;
	}

	public void setInfo(byte[] info) {
		this.info = info;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
		this.length = Utils.IntToBytes4(data.length);
	}
	
}
