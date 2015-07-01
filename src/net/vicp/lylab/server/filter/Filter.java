package net.vicp.lylab.server.filter;

import net.vicp.lylab.utils.internet.LYSocket;

public interface Filter {
	public byte[] doFilter(LYSocket socket, byte[] request);
	
}
