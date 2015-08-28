package net.vicp.lylab.core.interfaces;

import java.net.Socket;

public interface Aop extends LifeCycle {
	
	/**
	 * do action
	 * @param client
	 * @param request
	 * @return
	 * byte you want to reply to client
	 */
	public byte[] doAction(Socket client, byte[] request, int offset);
	/**
	 * You should set filters when initialize
	 * @param client
	 * @param request
	 * @return
	 */
	public void initialize();
	
	public Aop setProtocol(Protocol protocol);
	
}
