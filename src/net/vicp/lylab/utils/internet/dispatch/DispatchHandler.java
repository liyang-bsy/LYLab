package net.vicp.lylab.utils.internet.dispatch;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

/**
 * DispatchHandler will help you dispatch request to server dispatch logic
 * 
 * @author Young
 * @since 2016.02.04
 *
 */
public final class DispatchHandler extends Task implements Initializable {
	private static final long serialVersionUID = -1968695102042408808L;

	Socket client;
	byte[] clientRequest;
	Session session;
	Dispatcher<? super Confirm, ? super Confirm> dispatcher;
	Protocol protocol;

	AutoGeneratePool<DispatchHandler> controller;

	public void handlerRequest(Socket client, byte[] clientRequest, Session session,
			Dispatcher<? super Confirm, ? super Confirm> dispatcher, Protocol protocol) {
		this.client = client;
		this.clientRequest = clientRequest;
		this.session = session;
		this.dispatcher = dispatcher;
		this.protocol = protocol;
		signal();
	}

	/**
	 * async mode
	 */
	@Override
	public void exec() {
		while (!isFinished()) {
			if (session == null) {
				await();
				continue;
			}
			byte[] response;
			try {
				response = DispatchExecutor.doResponse(client, clientRequest, session, dispatcher, protocol);
			} catch (Exception e) {
				log.error("Dispatcher report an error:" + Utils.getStringFromException(e));
				continue;
			}
			try {
				DispatchExecutor.send(client, session, response);
			} catch (Exception e) {
				log.error("Sender report an error:" + Utils.getStringFromException(e));
			}
			session = null;
			controller.recycle(this);
		}
	}

	@Override
	public void initialize() {
		this.begin("Dispatch Handler");
	}

	public void setController(AutoGeneratePool<DispatchHandler> controller) {
		this.controller = controller;
	}

}
