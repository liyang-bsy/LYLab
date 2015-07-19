package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.internet.BaseSocket;

public interface Aop {
	public Message doAction(BaseSocket client, Message request);
}
