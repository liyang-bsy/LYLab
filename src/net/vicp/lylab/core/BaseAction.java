package net.vicp.lylab.core;

import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.utils.internet.LYSocket;
import net.vicp.lylab.utils.internet.impl.Message;

/**
 * BaseAction is a abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class BaseAction extends CloneableBaseObject implements Executor {
	
	protected LYSocket socket;
	protected Message request;
	protected Message response;

	public BaseAction() { }
	
	public BaseAction(Message request, Message response)
	{
		this.request = request;
		this.response = response;
	}
	
	public BaseAction(BaseAction batonPass)
	{
		this(batonPass.getRequest(), batonPass.getResponse());
	}

	public Message getRequest() {
		return request;
	}

	public void setRequest(Message request) {
		this.request = request;
	}

	public Message getResponse() {
		return response;
	}

	public void setResponse(Message response) {
		this.response = response;
	}

	public void setSocket(LYSocket socket) {
		this.socket = socket;
	}

}
