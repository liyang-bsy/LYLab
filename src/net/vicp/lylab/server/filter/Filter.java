package net.vicp.lylab.server.filter;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.internet.TaskSocket;
import net.vicp.lylab.utils.internet.impl.Message;

public interface Filter {
	public Message doFilter(TaskSocket socket, Message request) throws LYException;
	
}
