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
public class RPCAction extends BaseAction {

	@Override
	public void exec() {
		// TODO
		ServerDispathcer serverDispathcer = new ServerDispathcer();
		CallContent cc = (CallContent) getRequest();
		List<String> ipList = new ArrayList<>();
		if(cc.isBroadcast())
			ipList = serverDispathcer.getAllAddress(cc.getServer(), cc.getProcedure());
		else
			ipList.add(serverDispathcer.getAddress(cc.getServer(), cc.getProcedure()));
		for(String ip:ipList)
		{
			;
		}
	}

}
