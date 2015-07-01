package net.vicp.lylab.core.interfaces;

public interface Protocol {

	public byte[] getHead();
	public byte[] getSplitSignal();

	public byte[] toBytes();
	public Transcode toObject();

	public byte[] getLength();
	public byte[] getInfo();
	public byte[] getData();
	
}
