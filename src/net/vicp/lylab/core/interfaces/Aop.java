package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.internet.async.BaseSocket;

public interface Aop {
	public byte[] enterAction(Protocol protocol, BaseSocket client, byte[] request);
	public Message doAction(BaseSocket client, Message request);
}
