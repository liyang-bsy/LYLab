package net.vicp.lylab.core.interfaces;

import java.net.Socket;

public interface Dispatcher<I extends Confirm, O extends Confirm> extends LifeCycle {
	
	/**
	 * do action
	 * @param client
	 * @param request
	 * @return
	 * Object you want to reply to client
	 */
	public O doAction(Socket client, I request);
	
}
