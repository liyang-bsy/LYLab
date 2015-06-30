package net.vicp.lylab.utils.internet.protocol;

import java.util.Arrays;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.TranscodeObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.ByteUtils;

public abstract class AbstractProtocol extends BaseObject {

//	protected final byte[] head = "LYLab".getBytes();
//	protected final byte[] splitSignal = new byte[] { -15 };
	public abstract byte[] getHead();
	public abstract byte[] getSplitSignal();
	
	protected byte[] dataLength;
	protected byte[] className;
	protected byte[] data;

	public AbstractProtocol(TranscodeObject tp) {
		this(tp.encode());
	}

	public AbstractProtocol(AbstractProtocol protocol) {
		this(protocol.getClassName(), protocol.getData());
	}
	
	public AbstractProtocol(byte[] className, byte[] data) {
		this.className = className;
		this.data = data;
		this.dataLength = ByteUtils.IntToBytes4(data.length);
	}
	
	public byte[] toBytes() {
		int size = getHead().length + getSplitSignal().length
				+ dataLength.length + getSplitSignal().length
				+ className.length + getSplitSignal().length
				+ data.length;
		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < getHead().length; j++)
			bytes[i++] = getHead()[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < dataLength.length; j++)
			bytes[i++] = dataLength[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < className.length; j++)
			bytes[i++] = className[j];
		for (int j = 0; j < getSplitSignal().length; j++)
			bytes[i++] = getSplitSignal()[j];
		for (int j = 0; j < data.length; j++)
			bytes[i++] = data[j];
		return bytes;
	}
	
	public boolean checkHead(byte[] bytes)
	{
		if (!Arrays.equals(bytes, getHead())) return false;
		return true;
	}

	@Override
	public String toString() {
		String description = "{className=" + transformClassName()
				+ ",dataLength=" + ByteUtils.Bytes4ToInt(dataLength)
				+ ",data=" + transformData() + "}";
		return description;
	}

	public String transformClassName() {
		return new String(className);
	}

	public String transformData() {
		try {
			return new String(data, CoreDef.CHARSET);
		} catch (Exception e) {
			throw new LYException("Can not encode data into " + CoreDef.CHARSET + " string");
		}
	}

	public abstract TranscodeObject decodeJsonDataToObject();
	
	public byte[] getDataLength() {
		return dataLength;
	}

	public void setDataLength(byte[] dataLength) {
		this.dataLength = dataLength;
	}

	public byte[] getClassName() {
		return className;
	}

	public void setClassName(byte[] className) {
		this.className = className;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
}
