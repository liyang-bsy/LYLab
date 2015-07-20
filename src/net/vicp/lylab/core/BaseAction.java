package net.vicp.lylab.core;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Executor;
import net.vicp.lylab.core.model.SimpleMessage;

/**
 * BaseAction is a abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class BaseAction extends CloneableBaseObject implements Executor {
	
	protected Socket clientSocket;
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

	public void setSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

}
