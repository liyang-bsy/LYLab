package net.vicp.lylab.server.rpc.connector;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.TaskSocket;

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
		ServerConnector serverConnector = new ServerConnector();
		try {
			RPCMessage req = (RPCMessage) getRequest();
			Protocol protocol = (Protocol) CoreDef.config.getObject("protocol");
			if (req.isBroadcast()) {
				List<Pair<String, Integer>> addrList = serverDispathcer.getAllAddress(req.getServer(), req.getProcedure());
				List<Pair<String, Message>> result = new ArrayList<>();
				for (Pair<String, Integer> addr : addrList) {
					TaskSocket socket = serverConnector.getConnection(addr.getLeft(), addr.getRight());
					byte[] nextReq = protocol.encode(req.getMessage());
					byte[] response = socket.request(nextReq);
					Message message = (Message) protocol.decode(response);
					result.add(new Pair<>(addr.getLeft(), message));
				}
				getResponse().getBody().put("ResultList", result);
			} else {
				Pair<String, Integer> addr = serverDispathcer.getRandomAddress(req.getServer(), req.getProcedure());
				TaskSocket socket = serverConnector.getConnection(addr.getLeft(), addr.getRight());
				byte[] nextReq = protocol.encode(req.getMessage());
				byte[] response = socket.request(nextReq);
				setResponse((Message) protocol.decode(response));
			}
		} catch (Throwable t) {
			log.fatal(Utils.getStringFromThrowable(t));
		}
	}

}
