package net.vicp.lylab.server.rpc.client;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.Message;

public class Caller extends NonCloneableBaseObject {
	Message call(RPCMessage content) {
		return new Message();
	}

}
