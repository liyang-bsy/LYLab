package net.vicp.lylab.server.rpc.connector;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.model.CallContent;

/**
 * Please configure it into action list, so that RCPDispatcherAop may found this action.
 * 
 * @author Young
 *
 */
public class RPCProcedureConnector extends BaseAction {

	@Override
	public void exec() {
		CallContent cc = (CallContent) getRequest();
		List<String> ipList = new ArrayList<>();
//		if(cc.isBroadcast())
//			ipList = ServerDispathcer
			
	}

}
