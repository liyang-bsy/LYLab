package net.vicp.lylab.core.interfaces;

public interface Transcode {
	public void encode(Protocol protocol, Object obj);
	public byte[] encode(Object obj);
	public Object decode();
	
}
