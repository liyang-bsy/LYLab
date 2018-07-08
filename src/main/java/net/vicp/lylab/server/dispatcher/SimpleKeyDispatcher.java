package net.vicp.lylab.server.dispatcher;

import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.AbstractAction;
import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.Utils;

public class SimpleKeyDispatcher<I extends Message> extends AbstractMessageDispatcher<I, Message> {
	
	protected Config actions = null;

	@Override
	protected Message newResponse() {
		return new Message();
	}
	
	/**
	 * 
	 * @param request
	 * @return null means no action was mapped to current request
	 */
	@Override
	protected BaseAction mapAction(I request) {
		try {
			return (BaseAction) actions.getNewInstance(request.getKey() + "Action");
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void logger(Message request, Message response) {
		log.debug("Access key:" + request.getKey() + "\nBefore:" + request + "\nAfter:" + response);
	}

	@Override
	protected void dispatcher(AbstractAction action, Socket client, I request, Message response) {
		// gain key from request
		String key = request.getKey();
		if (StringUtils.isBlank(key)) {
			response.setCode(0x00000005);
			response.setMessage("Key not found");
			return;
		}
		// get action related to key
		action = mapAction(request);
		if (action == null) {
			response.setCode(0x00000006);
			response.setMessage("Action not found");
			return;
		}

		// Initialize action
		action.setSocket(client);
		action.setRequest(request);
		action.setResponse(response);
		try {
			// do action
			action.doAction();
		} catch (Throwable t) {
			String reason = Utils.getStringFromThrowable(t);
			log.error(reason);
			response.setCode(0x00000007);
			response.setMessage("Action execute failed:" + reason);
		}
	}

	public Config getActions() {
		return actions;
	}

	public void setActions(Config actions) {
		this.actions = actions;
	}

}