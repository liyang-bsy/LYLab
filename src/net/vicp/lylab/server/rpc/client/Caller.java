package net.vicp.lylab.server.rpc.client;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.model.CallContent;
import net.vicp.lylab.core.model.Message;

public class Caller extends NonCloneableBaseObject {
	Message call(CallContent content) {
		return new Message();
	}

}
