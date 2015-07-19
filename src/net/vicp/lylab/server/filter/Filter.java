package net.vicp.lylab.server.filter;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.internet.BaseSocket;

public interface Filter {
	public Message doFilter(BaseSocket socket, Message request) throws LYException;
	
}
