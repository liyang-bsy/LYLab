package net.vicp.lylab.core;

import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.core.model.SimpleMessage;
import net.vicp.lylab.utils.internet.BaseSocket;

/**
 * BaseAction is a abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class BaseAction extends CloneableBaseObject implements Executor {
	
	protected BaseSocket socket;
	protected SimpleMessage request;
	protected SimpleMessage response;

	public BaseAction() { }
	
	public BaseAction(SimpleMessage request, SimpleMessage response)
	{
		this.request = request;
		this.response = response;
	}
	
	public BaseAction(BaseAction batonPass)
	{
		this(batonPass.getRequest(), batonPass.getResponse());
	}

	public SimpleMessage getRequest() {
		return request;
	}

	public void setRequest(SimpleMessage request) {
		this.request = request;
	}

	public SimpleMessage getResponse() {
		return response;
	}

	public void setResponse(SimpleMessage response) {
		this.response = response;
	}

	public void setSocket(BaseSocket socket) {
		this.socket = socket;
	}

}
