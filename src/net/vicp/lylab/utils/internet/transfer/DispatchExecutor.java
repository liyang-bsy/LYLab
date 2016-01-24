package net.vicp.lylab.utils.internet.transfer;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.utils.internet.Session;
import net.vicp.lylab.utils.tq.Task;

/**
 * DispatchHandler will help you dispatch request to server dispatch logic
 * 
 * @author Young
 * @since 2016.01.24
 *
 */
public class DispatchExecutor<I, O> extends Task {
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

	@SuppressWarnings("unchecked")
	@Override
	public void exec() {
		byte[] response = null;
		if (dispatcher != null || protocol == null)
			response = clientRequest;
		else {
			Object request = protocol.decode(clientRequest, 0);
			if (request instanceof HeartBeat)
				response = protocol.encode(request);
			else
				response = protocol.encode(dispatcher.doAction(client, (I) request));
		}
		session.send(client, response);
	}

}
