package net.vicp.lylab.server.filter;

import java.net.Socket;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.model.Message;

public interface Filter {
	public Message doFilter(Socket client, Message request) throws LYException;
	
}
