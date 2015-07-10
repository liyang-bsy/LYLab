package net.vicp.lylab.server.aop;

import java.net.ServerSocket;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.internet.impl.Message;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

import org.apache.commons.lang3.StringUtils;

public class DoActionLong extends ToClientLongSocket {
	private static final long serialVersionUID = -8400721992403701180L;

	public DoActionLong(ServerSocket serverSocket, byte[] heartBeat) {
		super(serverSocket, heartBeat);
	}

	protected static Config config;
	protected static Filter[] startChain;
	protected static Filter[] afterChain;

	@SuppressWarnings("unchecked")
	@Override
	public byte[] response(byte[] request) {
		byte[] tmp = null;
		// do start filter
		if (startChain != null && startChain.length != 0)
			for (Filter filter : startChain)
				if ((tmp = filter.doFilter(this, request)) != null)
					return tmp;
		
		Message msg = null;
		Message response = new Message();
		String key = null;
		BaseAction action = null;
		Protocol protocol = null;
		try {
			do {
				try {
					protocol = ProtocolUtils.fromBytes(bufferProtocol, request);
				} catch (Exception e) {
					break;
				}
				try {
					msg = (Message) protocol.toObject();
				} catch (Exception e) {
					response.setCode(0x00002);
					response.setMessage("Message not found");
					log.debug(Utils.getStringFromException(e));
					break;
				}
				// gain key from request
				key = msg.getKey();
				if (StringUtils.isBlank(key)) {
					response.setCode(0x00003);
					response.setMessage("Key not found");
					break;
				}
				response.setKey(key);
				// get action related to key
				try {
					action = new AtomicStrongReference<BaseAction>().get((Class<BaseAction>) Class.forName(getConfig().getString(key + "Action")));
				} catch (Exception e) { }
				if (action == null) {
					response.setCode(0x00004);
					response.setMessage("Action not found");
					break;
				}
				// Initialize action
				action.setRequest(msg);
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
		log.debug("Access key:" + key  + "\nBefore:" + msg + "\nAfter:" + response);
		return protocol == null ? null : protocol.encode(response).toBytes();
	}

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		DoActionLong.config = config;
	}

}