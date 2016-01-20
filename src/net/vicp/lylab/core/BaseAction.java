package net.vicp.lylab.core;

import net.vicp.lylab.core.model.Message;

/**
 * BaseAction from abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class BaseAction extends AbstractAction {

	@Override
	public Message getRequest() {
		return (Message) super.getRequest();
	}

	@Override
	public Message getResponse() {
		return (Message) super.getResponse();
	}

}
