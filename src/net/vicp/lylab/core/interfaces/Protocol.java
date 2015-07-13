package net.vicp.lylab.core.interfaces;

public interface Protocol {
	public byte[] getHead();
	public byte[] getSplitSignal();

	public byte[] encode(Object obj);
	public Object decode(byte[] bytes);
	public boolean validate(byte[] bytes, int len);

}
