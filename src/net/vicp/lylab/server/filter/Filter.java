package net.vicp.lylab.server.filter;

import java.net.Socket;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleMessage;

public interface Filter<I extends O, O extends SimpleMessage> extends LifeCycle {
	public O doFilter(Socket client, I request) throws LYException;
	
}
