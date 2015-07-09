package net.vicp.lylab.core.interfaces;

public interface Transcode {
	public Protocol encode();
	public Object decode(Protocol protocol);
	
}
