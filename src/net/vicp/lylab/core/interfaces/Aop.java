package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.utils.internet.async.BaseSocket;
import net.vicp.lylab.utils.internet.impl.Message;

public interface Aop {
	public byte[] enterAction(Protocol protocol, BaseSocket client, byte[] request);
	public Message doAction(BaseSocket client, Message request);
}
