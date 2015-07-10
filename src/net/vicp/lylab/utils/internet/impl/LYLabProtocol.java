package net.vicp.lylab.utils.internet.impl;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transcode;
import net.vicp.lylab.utils.Utils;

/**
 * A self-defined protocol easy transfer Objects through socket.<br>
 * Data will be transfered as JSON string.
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
	public Protocol encode(Object obj)
	{
		try {
			return new LYLabProtocol(obj.getClass(), Utils.serialize(obj).getBytes(CoreDef.CHARSET));
		} catch (Exception e) { throw new LYException("Encode failed", e); }
	}

	@Override
	public Object decode()
	{
		if(getInfo() == null)
			throw new LYException("Inner info is null");
		if(getData() == null)
			throw new LYException("Inner data is null");
		try {
			return Utils.deserialize(Class.forName(new String(getInfo()))
					, new String(getData(), CoreDef.CHARSET));  
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + new String(getInfo()) + ". Maybe the data isn't json?", e);
		}
	}
	
	@Override
	public void setAll(byte[] info, byte[] data) {
		this.info = info;
		this.data = data;
		this.length = Utils.IntToBytes4(data.length);
	}

	@Override
	public Transcode toObject()
	{
		if(getInfo() == null)
			throw new LYException("Inner info is null");
		if(getData() == null)
			throw new LYException("Inner data is null");
		try {
			return (Transcode) Utils.deserialize(Class.forName(new String(getInfo())), new String(getData(), CoreDef.CHARSET));
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + new String(getInfo()) + ". Maybe the data isn't json?", e);
		}
	}

	@Override
	public byte[] toBytes() {
		int size = getHead().length + getSplitSignal().length
				+ length.length + getSplitSignal().length
				+ info.length + getSplitSignal().length
				+ data.length;
		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < getHead().length; j++)
			bytes[i++] = getHead()[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < getLength().length; j++)
			bytes[i++] = getLength()[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < getInfo().length; j++)
			bytes[i++] = getInfo()[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < getData().length; j++)
			bytes[i++] = getData()[j];
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
