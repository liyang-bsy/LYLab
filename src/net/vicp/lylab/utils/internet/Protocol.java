package net.vicp.lylab.utils.internet;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import flexjson.JSONDeserializer;
import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.Algorithm;
import net.vicp.lylab.utils.ByteUtils;

public class Protocol extends BaseObject {

	public Protocol() {
		this(new byte[] { }, new byte[] { }, new byte[4]);
	}

	public Protocol(Protocol protocol) {
		this(protocol.getClassName(), protocol.getData(), protocol.getDataLength());
	}
	
	public Protocol(Class<?> clazz) {
		this(clazz.getName().getBytes(), new byte[] { }, new byte[4]);
	}

	public Protocol(Class<?> clazz, byte[] data, int dataLength) {
		this(clazz.getName().getBytes(), data, ByteUtils.IntToBytes4(dataLength));
	}
	
	public Protocol(Class<?> clazz, byte[] data, byte[] dataLength) {
		this(clazz.getName().getBytes(), dataLength, data);
	}
	
	public Protocol(byte[] className, byte[] data, byte[] dataLength) {
		this.dataLength = dataLength;
		this.className = className;
		this.data = data;
	}
	
	@Override
	public String toString() {
		try {
			String description = "{className=" + new String(className)
					+ ",dataLength=" + ByteUtils.Bytes4ToInt(dataLength)
					+ ",data=" + new String(data, "UTF-8") + "}";
			if(new String(data, "UTF-8").equals("rew"))
				throw new LYException("???");
			return description;
		} catch (Exception e) {
			throw new LYException("Can't encoding data into UTF-8", e);
		}
	}

	public byte[] toBytes() {
		int size = head.length + splitSignal.length
				+ dataLength.length + splitSignal.length
				+ className.length + splitSignal.length
				+ data.length;
		byte[] bytes = new byte[size];
		int i = 0;
		for (int j = 0; j < head.length; j++)
			bytes[i++] = head[j];
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
		byte[] temp = Arrays.copyOfRange(bytes, 0, head.length);
		if (!Arrays.equals(temp, head))
			return null;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, splitSignal);
		if (headEndPosition != head.length) return null;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + splitSignal.length, bytes.length);
		int dataLengthEndPosition = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		byte[] dataLength = Arrays.copyOfRange(bytes, head.length + splitSignal.length, dataLengthEndPosition);
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + splitSignal.length + Algorithm.KMPSearch(temp, splitSignal);
		if (classNameEndPosition <= 0) return null;
		byte[] className = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, classNameEndPosition);

		byte[] data = Arrays.copyOfRange(bytes, classNameEndPosition + splitSignal.length, bytes.length);

		return new Protocol(className, data, dataLength);
	}

	public static int validate(byte[] bytes, int len) {
		if (bytes == null || bytes.length <= 12) return -1;
		byte[] temp = Arrays.copyOfRange(bytes, 0, head.length);
		if (!Arrays.equals(temp, head))
			return -1;
		
		int headEndPosition = Algorithm.KMPSearch(bytes, splitSignal);
		if (headEndPosition != head.length) return -1;
		
		temp = Arrays.copyOfRange(bytes, headEndPosition + splitSignal.length, bytes.length);
		int dataLengthEndPosition = head.length + splitSignal.length + CoreDef.SIZEOF_INTEGER;
		int dataLength = ByteUtils.Bytes4ToInt(Arrays.copyOfRange(bytes, head.length + splitSignal.length, dataLengthEndPosition));
		
		temp = Arrays.copyOfRange(bytes, dataLengthEndPosition + splitSignal.length, bytes.length);
		int classNameEndPosition = dataLengthEndPosition + splitSignal.length + Algorithm.KMPSearch(temp, splitSignal);
		if (classNameEndPosition <= 0) return -1;

		if(len > dataLength + classNameEndPosition + splitSignal.length)
			return -1;
		if(len < dataLength + classNameEndPosition + splitSignal.length)
			return 1;
		return 0;
	}

	public Object convertToObject()
	{
		if(className == null || data == null)
			throw new LYException("Inner data or className is null");
		try {
			return new JSONDeserializer<Object>().use(null, Class.forName(new String(className))).deserialize(new String(getData(), "UTF-8"));
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + new String(className), e);
		}
	}

	protected static final byte[] head = "LYLab".getBytes();
	protected static final byte[] splitSignal = new byte[] { -15 };
	protected byte[] dataLength;
	protected byte[] className;
	protected byte[] data;

	public String transformClassName() {
		return new String(className);
	}

	public String transformData() {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new LYException("Can not convert data into utf-8 string");
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
