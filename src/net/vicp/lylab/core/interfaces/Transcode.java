package net.vicp.lylab.core.interfaces;

public interface Transcode {
	public Protocol encode(Object obj);
	public Object decode();
	
}
