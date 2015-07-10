package net.vicp.lylab.core.interfaces;

public interface Protocol extends Transcode {

	public void setAll(byte[] info, byte[] data);
	
	public byte[] getHead();
	public byte[] getSplitSignal();

	public byte[] toBytes();
	public Object toObject();

	public byte[] getLength();
	public byte[] getInfo();
	public byte[] getData();
	
}
