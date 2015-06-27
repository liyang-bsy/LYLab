package net.vicp.lylab.utils.internet;

import java.util.Arrays;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.utils.Algorithm;

public class Protocol extends BaseObject {

	public Protocol()
	{
		this.head = new byte[4];
		this.info = new byte[4];
		this.data = new byte[4];
	}
	
	public Protocol(byte[] head, byte[] info, byte[] data)
	{
		this.head = head;
		this.info = info;
		this.data = data;
	}

	public byte[] toBytes()
	{
		int size = head.length + splitSignal.length + info.length + splitSignal.length + splitSignal.length + data.length;
		byte[] bytes = new byte[size];
		int i=0;
		for(int j=0;j<head.length;j++)
			bytes[i++] = head[j];
		for(int j=0;j<splitSignal.length;j++)
			bytes[i++] = head[j];
		for(int j=0;j<info.length;j++)
			bytes[i++] = head[j];
		for(int j=0;j<splitSignal.length;j++)
			bytes[i++] = head[j];
		for(int j=0;j<splitSignal.length;j++)
			bytes[i++] = head[j];
		for(int j=0;j<data.length;j++)
			bytes[i++] = head[j];
		return bytes;
	}

	public Protocol fromBytes(byte[] bytes)
	{
		int infoPosition = Algorithm.KMPSearch(bytes, splitSignal);
		int dataPosition = Algorithm.KMPSearch(bytes, splitSignal);
		byte[] head = Arrays.copyOfRange(bytes, 0, infoPosition - splitSignal.length);
		byte[] info = Arrays.copyOfRange(bytes, infoPosition, dataPosition - splitSignal.length - splitSignal.length);
		byte[] data = Arrays.copyOfRange(bytes, dataPosition, bytes.length);
		return new Protocol(head, info, data);
	}
	
	protected byte[] head;
	protected byte[] info;
	protected byte[] data;
	
	protected byte[] splitSignal = "\r\n".getBytes();

	public byte[] getHead() {
		return head;
	}

	public void setHead(byte[] head) {
		this.head = head;
	}

	public byte[] getInfo() {
		return info;
	}

	public void setInfo(byte[] info) {
		this.info = info;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
