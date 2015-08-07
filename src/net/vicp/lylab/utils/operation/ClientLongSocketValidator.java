package net.vicp.lylab.utils.operation;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AdditionalOp;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.internet.TaskSocket;

public class ClientLongSocketValidator extends NonCloneableBaseObject implements AdditionalOp<ClientLongSocket> {

	@Override
	public boolean operate(ClientLongSocket item) {
		((TaskSocket) item).connect();
		return true;
	}

}
