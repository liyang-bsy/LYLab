package net.vicp.lylab.utils.internet.dispatch;

import java.io.IOException;
import java.net.Socket;

import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

/**
 * DispatchHandler will help you dispatch request to server dispatch logic
 * 
 * @author Young
 * @since 2016.01.24
 *
 */
public final class DispatchExecutor extends Task {
	private static final long serialVersionUID = -8759689034880271599L;

	Socket client;
	byte[] clientRequest;
	Session session;
	Dispatcher<? super Confirm, ? super Confirm> dispatcher;
	Protocol protocol;

	public DispatchExecutor(Socket client, byte[] clientRequest, Session session, Dispatcher<? super Confirm, ? super Confirm> dispatcher, Protocol protocol) {
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
		byte[] response = doResponse(client, clientRequest, session, dispatcher, protocol);
		send(client, session, response);
	}

	/**
	 * sync mode
	 */
	public final static byte[] doResponse(Socket client, byte[] clientRequest, Session session, Dispatcher<? super Confirm, ? super Confirm> dispatcher, Protocol protocol) {
		byte[] response = null;
		if (dispatcher == null || protocol == null)
			response = clientRequest;
		else {
			Confirm request = protocol.decode(clientRequest, 0);
			if (request instanceof HeartBeat)
				response = protocol.encode(request);
			else
				response = protocol.encode(dispatcher.doAction(client, request));
		}
		return response;
	}

	public final static void send(Socket client, Session session, byte[] response) {
		session.send(client, response);
	}

	public final void close(Socket client) {
		try {
			client.close();
		} catch (IOException e) {
			log.error("Socket close failed:" + Utils.getStringFromException(e));
		}
	}
	
	public final void sendAndClose(Socket client, Session session, byte[] response) {
		send(client, session, response);
		close(client);
	}

}
