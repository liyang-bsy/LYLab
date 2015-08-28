package net.vicp.lylab.server.aop;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;

import org.apache.commons.lang3.StringUtils;

public class DefaultAop extends NonCloneableBaseObject implements Aop {
	protected List<Filter> filterChain = new ArrayList<Filter>();
	protected Protocol protocol= null;
	
	@Override
	public void initialize() {
		if(protocol == null)
			throw new LYException("No available protocol");
		filterChain.clear();
		for (String key : CoreDef.config.getConfig("Filters").keyList()) {
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
	public byte[] doAction(Socket client, byte[] requestByte, int offset) {
		Message request = null;

		String key = null;
		BaseAction action = null;
		Message response = new Message();
		try {
			do {
				try {
					Object obj = protocol.decode(requestByte, offset);
					if(obj instanceof HeartBeat)
						return protocol.encode(obj);
					request = (Message) obj;
				} catch (Exception e) {
					log.debug(Utils.getStringFromException(e));
				}
				if(request == null) {
					response.setCode(0x00001);
					response.setMessage("Message not found");
					break;
				}
				// do start filter
				if (filterChain != null && filterChain.size() != 0)
					for (Filter filter : filterChain) {
						Message ret = null;
						if ((ret = filter.doFilter(client, request)) != null)
							return protocol.encode(ret);
					}
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
		return protocol.encode(response);
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public Aop setProtocol(Protocol protocol) {
		this.protocol = protocol;
		return this;
	}

}