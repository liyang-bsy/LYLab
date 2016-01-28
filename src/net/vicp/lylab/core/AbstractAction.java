package net.vicp.lylab.core;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Executor;

/**
 * An abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class AbstractAction extends CloneableBaseObject implements Executor {

	protected Socket clientSocket;
	protected Object request;
	protected Object response;

	public AbstractAction() { }

	public void doAction() {
		exec();
	}

	public AbstractAction(Object request, Object response) {
		this.request = request;
		this.response = response;
	}

	public AbstractAction(AbstractAction batonPass) {
		this(batonPass.getRequest(), batonPass.getResponse());
	}

	public Object getRequest() {
		return request;
	}

	public void setRequest(Object request) {
		this.request = request;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public void setSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

}
