package net.vicp.lylab.core.interfaces;

import java.net.Socket;

import net.vicp.lylab.core.model.Message;

public interface Aop extends LifeCycle {
	
	/**
	 * do action
	 * @param client
	 * @param request
	 * @return
	 * Message you want to reply to client
	 */
	public Message doAction(Socket client, Message request);
	/**
	 * You should set filters when initialize
	 * @param client
	 * @param request
	 * @return
	 */
	public void initialize();
	
}
