package net.vicp.lylab.server.filter;

import net.vicp.lylab.utils.internet.LYSocket;
import net.vicp.lylab.utils.internet.impl.Message;

public interface Filter {
	public Message doFilter(LYSocket socket, Message request);
	
}
