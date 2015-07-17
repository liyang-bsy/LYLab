package net.vicp.lylab.core.interfaces;

public interface Protocol {
	public byte[] getHead();
	public byte[] getSplitSignal();

	public byte[] encode(Object obj);
	public Object decode(byte[] bytes);

	/**
	 * Validate if these bytes could be assemble into a protocol
	 * @param protocol
	 * @param bytes
	 * @param len
	 * @return
	 * 0 yes, -1 no, 1 not enough
	 */
	public boolean validate(byte[] bytes, int len);

}
