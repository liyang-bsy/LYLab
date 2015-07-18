package net.vicp.lylab.server.filter;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.internet.async.BaseSocket;
import net.vicp.lylab.utils.internet.impl.SimpleMessage;

public interface Filter {
	public SimpleMessage doFilter(BaseSocket socket, SimpleMessage request) throws LYException;
	
}
