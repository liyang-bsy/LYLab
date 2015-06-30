package net.vicp.lylab.core.interfaces;

public interface Transcode {

	public Protocol encode();
	public String[] exclude(String ... excludeRule);
	public Object decode(Protocol protocol);
	
}
