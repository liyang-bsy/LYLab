package net.vicp.lylab.utils.internet;

import java.util.Arrays;

import flexjson.JSONDeserializer;
import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.TranscodeProtocol;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.ByteUtils;

public class Protocol extends BaseObject {

	protected static final byte[] head = "LYLab".getBytes();
	protected static final byte[] splitSignal = new byte[] { -15 };
	protected byte[] dataLength;
	protected byte[] className;
	protected byte[] data;
	
	public Protocol() {
		this(new byte[] { }, new byte[] { });
	}
	
	public Protocol(TranscodeProtocol tp) {
		this(tp.encode());
	}

	public Protocol(Protocol protocol) {
		this(protocol.getClassName(), protocol.getData());
	}
	
	public Protocol(Class<?> clazz, byte[] data) {
		this(clazz.getName().getBytes(), data);
	}
	
	public Protocol(byte[] className, byte[] data) {
		this.className = className;
		this.data = data;
		this.dataLength = ByteUtils.IntToBytes4(data.length);
	}

	public byte[] toBytes() {
		int size = getHead().length + splitSignal.length
				+ dataLength.length + splitSignal.length
				+ className.length + splitSignal.length
				+ data.length;
		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < getHead().length; j++)
			bytes[i++] = getHead()[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < dataLength.length; j++)
			bytes[i++] = dataLength[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < className.length; j++)
			bytes[i++] = className[j];
		for (int j = 0; j < splitSignal.length; j++)
			bytes[i++] = splitSignal[j];
		for (int j = 0; j < data.length; j++)
			bytes[i++] = data[j];
		return bytes;
	}

	public static Protocol fromBytes(byte[] bytes) {
		if (bytes == null || bytes.length <= 12) return null;
		byte[] temp = Arrays.copyOfRange(bytes, 0, getHead().length);
		if (!Arrays.equals(temp, getHead())) return null;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, splitSignal);
		if (headEndPosition != getHead().length) return null;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + splitSignal.length, bytes.length);
		int dataLengthEndPosition = getHead().length + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		
		int dataLength = ByteUtils.Bytes4ToInt(Arrays.copyOfRange(bytes, getHead().length + splitSignal.length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + splitSignal.length + Algorithm.KMPSearch(temp, splitSignal);
		if (classNameEndPosition <= 0) return null;
		byte[] className = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, classNameEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, classNameEndPosition + splitSignal.length, classNameEndPosition + splitSignal.length + dataLength);

		return new Protocol(className, data);
	}

	public static int validate(byte[] bytes, int len) {
		if (bytes == null || bytes.length <= 12) return -1;
		byte[] temp = Arrays.copyOfRange(bytes, 0, getHead().length);
		if (!Arrays.equals(temp, getHead()))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, splitSignal);
		if (headEndPosition != getHead().length) return -1;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + splitSignal.length, bytes.length);
		int dataLengthEndPosition = getHead().length + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int dataLength = ByteUtils.Bytes4ToInt(Arrays.copyOfRange(bytes, getHead().length + splitSignal.length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + splitSignal.length + Algorithm.KMPSearch(temp, splitSignal);
		if (classNameEndPosition <= 0) return -1;

		if(len > dataLength + classNameEndPosition + splitSignal.length)
			return -1;
		if(len < dataLength + classNameEndPosition + splitSignal.length)
			return 1;
		return 0;
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
			return new String(data, "UTF-8");
		} catch (Exception e) {
			throw new LYException("Can not encode data into utf-8 string");
		}
	}

	public Object decodeJsonDataToObject()
	{
		Protocol protocol = this;
		if(protocol.getClassName() == null)
			throw new LYException("Inner className is null");
		if(protocol.getData() == null)
			throw new LYException("Inner data is null");
		try {
			return new JSONDeserializer<Object>().use(null, Class.forName(protocol.transformClassName())).deserialize(protocol.transformData());
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + protocol.transformClassName() + ". Maybe the data isn't json?", e);
		}
	}
	
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

	public static byte[] getHead() {
		return head;
	}

	public static byte[] getSplitsignal() {
		return splitSignal;
	}

}
