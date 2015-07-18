package net.vicp.lylab.server.aop;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.internet.async.BaseSocket;
import net.vicp.lylab.utils.internet.impl.SimpleMessage;

public class AsyncDoAction extends NonCloneableBaseObject implements Aop {

	protected static Config config;
	protected static List<Filter> filterChain;
	
	@Override
	public byte[] enterAction(Protocol protocol, BaseSocket client, byte[] request) {
		SimpleMessage msg = null;
		SimpleMessage response = null;
		try {
			msg = (SimpleMessage) protocol.decode(request);
		} catch (Exception e) {
			log.debug(Utils.getStringFromException(e));
		}
		if(msg == null) {
			response = new SimpleMessage();
			response.setCode(0x00001);
			response.setMessage("Message not found");
		}
		else
			response = doAction(client, msg);
		return protocol == null ? null : protocol.encode(response);
	}
	
	@Override
	public SimpleMessage doAction(BaseSocket client, SimpleMessage request) {
		String key = null;
		BaseAction action = null;
		SimpleMessage response = null;
		// do start filter
		if (filterChain != null && filterChain.size() != 0)
			for (Filter filter : filterChain)
				if ((response = filter.doFilter(client, request)) != null)
					return response;
		response = new SimpleMessage();
		try {
			do {
				// gain key from request
				key = request.getKey();
				if (StringUtils.isBlank(key)) {
					response.setCode(0x00002);
					response.setMessage("Key not found");
					break;
				}
				response.setKey(key);
				// get action related to key
				try {
					action = (BaseAction) Class.forName(getConfig().getString(key + "Action")).newInstance();
				} catch (Exception e) { }
				if (action == null) {
					response.setCode(0x00003);
					response.setMessage("Action not found");
					break;
				}
				// Initialize action
				action.setSocket(client);
				action.setRequest(request);
				action.setResponse(response);
				// execute action
				try {
					action.exec();
				} catch (Throwable t) {
					log.error(Utils.getStringFromThrowable(t));
				}
			} while (false);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		// to logger
		log.debug("Access key:" + key  + "\nBefore:" + request + "\nAfter:" + response);
		return response;
	}

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		AsyncDoAction.config = config;
	}

	public static List<Filter> getFilterChain() {
		return filterChain;
	}

	public static void setFilterChain(List<Filter> filterChain) {
		AsyncDoAction.filterChain = filterChain;
	}

}
