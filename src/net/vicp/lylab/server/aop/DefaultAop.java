package net.vicp.lylab.server.aop;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;

import org.apache.commons.lang3.StringUtils;

public class DefaultAop extends NonCloneableBaseObject implements Aop {
	protected List<Filter> filterChain;

	@Override
	public void initialize() {
		filterChain = new ArrayList<Filter>();

		Set<String> keySet = CoreDef.config.getConfig("Filters").keySet();
		for (String key : keySet) {
			try {
				Class<?> instanceClass = Class.forName(CoreDef.config.getConfig("Filters").getString(key));
				Filter tmp = (Filter) instanceClass.newInstance();
				filterChain.add(tmp);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	@Override
	public void close() throws Exception {
		for (Filter filter : filterChain)
			filter.close();
	}
	
	@Override
	public Message doAction(Socket client, Message request) {
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
					action = (BaseAction) Class.forName(CoreDef.config.getConfig("Aop").getString(key + "Action")).newInstance();
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

}