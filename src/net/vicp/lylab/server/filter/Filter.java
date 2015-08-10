package net.vicp.lylab.server.filter;

import java.net.Socket;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.Message;

public interface Filter extends LifeCycle {
	public Message doFilter(Socket client, Message request) throws LYException;
	
}
