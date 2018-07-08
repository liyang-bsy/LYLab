package net.vicp.lylab.server.dispatcher;

import java.net.Socket;

import net.vicp.lylab.core.AbstractAction;
import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.model.SimpleMessage;
import net.vicp.lylab.utils.Utils;

public abstract class AbstractMessageDispatcher<I extends O, O extends SimpleMessage> extends AbstractDispatcher<I, O> implements Dispatcher<I, O> {

	protected abstract void dispatcher(AbstractAction action, Socket client, I request, O response);

	/**
	 * 
	 * @param request
	 * @return null means no action was mapped to current request
	 */
	protected abstract AbstractAction mapAction(I request);
	
	// Java language defect
	protected abstract O newResponse();

	@Override
	public O doAction(Socket client, I request) {
		BaseAction action = null;
		O response = newResponse();
		try {
			do {
				// decode nothing
				if (request == null) {
					response.setCode(0x00000002);
					response.setMessage("Request not found");
					break;
				}
				// filter chain
				try {
					O tmp = filterChain(client, request);
					if (tmp != null) {
						response = tmp;
						break;
					}
				} catch (Exception e) {
					response.setCode(0x00000003);
					response.setMessage("Filter chain runtime error");
					break;
				}
				// sync response and request
				response.copyBasicInfo(request);

				try {
					dispatcher(action, client, request, response);
				} catch (Throwable t) {
					String reason = Utils.getStringFromThrowable(t);
					log.error(reason);
					response.setCode(0x00000004);
					response.setMessage("Action dispatch/execute failed:" + reason);
				}
			} while (false);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		try {
			// save access log
			logger(request, response);
		} catch (Exception e) {
			log.error("Logger failed:" + Utils.getStringFromException(e));
		}
		return response;
	}

}