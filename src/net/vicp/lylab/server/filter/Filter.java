package net.vicp.lylab.server.filter;

import java.net.Socket;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;

public interface Filter<I, O> extends LifeCycle {
	public O doFilter(Socket client, I request) throws LYException;
	
}
