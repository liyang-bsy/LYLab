package net.vicp.lylab.utils.internet;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.utils.tq.Task;

/**
 * DispatchHandler will help you dispatch request to server dispatch logic
 * 
 * @author Young
 * @since 2016.01.24
 *
 */
public final class DispatchExecutor<I extends Confirm, O extends Confirm> extends Task {
	private static final long serialVersionUID = -8759689034880271599L;

	Socket client;
	byte[] clientRequest;
	Session session;
	Dispatcher<I, O> dispatcher;
	Protocol protocol;

	public DispatchExecutor(Socket client, byte[] clientRequest, Session session, Dispatcher<I, O> dispatcher, Protocol protocol) {
		this.client = client;
		this.clientRequest = clientRequest;
		this.session = session;
		this.dispatcher = dispatcher;
		this.protocol = protocol;
	}

	/**
	 * async mode
	 */
	@Override
	public void exec() {
		send(doResponse());
	}

	/**
	 * sync mode
	 */
	@SuppressWarnings("unchecked")
	public byte[] doResponse() {
		byte[] response = null;
		if (dispatcher == null || protocol == null)
			response = clientRequest;
		else {
			Confirm request = protocol.decode(clientRequest, 0);
			if (request instanceof HeartBeat)
				response = protocol.encode(request);
			else
				response = protocol.encode(dispatcher.doAction(client, (I) request));
		}
		return response;
	}

	public void send(byte[] response) {
		session.send(client, response);
	}

}
