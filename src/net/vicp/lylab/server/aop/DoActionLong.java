package net.vicp.lylab.server.aop;

import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.internet.BaseSocket;

import org.apache.commons.lang3.StringUtils;

public class DoActionLong extends NonCloneableBaseObject implements Aop {
	protected static Config config;
	protected static List<Filter> filterChain;

	@Override
	public Message doAction(BaseSocket client, Message request) {
		String key = null;
		BaseAction action = null;
		Message response = null;
		// do start filter
		if (filterChain != null && filterChain.size() != 0)
			for (Filter filter : filterChain)
				if ((response = filter.doFilter(client, request)) != null)
					return response;
		response = new Message();
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
		DoAction.config = config;
	}

	public static List<Filter> getFilterChain() {
		return filterChain;
	}

	public static void setFilterChain(List<Filter> filterChain) {
		DoActionLong.filterChain = filterChain;
	}

}