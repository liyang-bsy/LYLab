package net.vicp.lylab.core;

import net.vicp.lylab.core.model.Message;

/**
 * BaseAction from abstract Action, Override exec() to do your service
 * @author Young
 *
 */
public abstract class BaseAction extends AbstractAction {

	protected String badParameter = "#NULL";

	public abstract boolean foundBadParameter();

	public void doAction() {
		if (foundBadParameter()) {
			getResponse().setCode(0x00000008);
			if(!badParameter.equals("#NULL"))
				getResponse().setMessage("Missing or bad parameter:" + badParameter);
			return;
		}
		exec();
	}

	@Override
	public Message getRequest() {
		return (Message) super.getRequest();
	}

	@Override
	public Message getResponse() {
		return (Message) super.getResponse();
	}

}
