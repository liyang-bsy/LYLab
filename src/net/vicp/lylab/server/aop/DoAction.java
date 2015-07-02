package net.vicp.lylab.server.aop;

import java.net.ServerSocket;
import java.util.Arrays;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.internet.impl.Message;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

import org.apache.commons.lang3.StringUtils;

public class DoAction extends ToClientLongSocket {
	private static final long serialVersionUID = -8400721992403701180L;

	public DoAction(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket, heartBeat);
	}

	protected static AtomicStrongReference<Config> config = new AtomicStrongReference<Config>();
	protected static Filter[] startChain;
	protected static Filter[] afterChain;

	@SuppressWarnings("unchecked")
	@Override
	public byte[] response(byte[] request) {
		byte[] tmp = null;
		if (startChain != null && startChain.length != 0)
			for (Filter filter : startChain)
				if ((tmp = filter.doFilter(this, request)) != null)
					return tmp;
		
		byte[] before = Arrays.copyOf(request, request.length);
		byte[] after = null;
		Message msg = null;
		Message response = new Message();
		String key = null;
		AtomicStrongReference<BaseAction> action = new AtomicStrongReference<BaseAction>();
		try {
			do {
				try {
					msg = (Message) ProtocolUtils.fromBytes(request).toObject();
				} catch (Exception e) {
					response.setCode(1);
					response.setMessage(Utils.getStringFromException(e));
					break;
				}
				// gain key from request
				key = msg.getKey();
				if (StringUtils.isBlank(key)) {
					response.setCode(0x00003);
					response.setMessage("JSON部分缺省");
					break;
				}
				// get action related to key
				try {
					action.get((Class<BaseAction>) Class.forName(getConfig().getString(key + "Action")));
				} catch (Exception e) { }
				if (action.get() == null) {
					response.setCode(0x00004);
					response.setMessage("协议未定义");
					break;
				}
				// Initialize action
				action.get().setRequest(msg);
				action.get().setResponse(response);
				// execute action
				action.get().exec();
				// extract response from action
				response = action.get().getResponse();
			} while (false);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		// save result
		byte[] result = response.encode().toBytes();
		after = Arrays.copyOf(result, result.length);

		// save result
		if (afterChain != null && afterChain.length != 0)
			for (Filter filter : afterChain)
				if ((tmp = filter.doFilter(this, request)) != null)
					return tmp;
		log.debug("Access key:" + key  + "\nBefore:" + ProtocolUtils.fromBytes(before) + "\nAfter:" + ProtocolUtils.fromBytes(after));
		return after;
	}

	public static Config getConfig() {
		//TODO
		return config.get(Config.class, System.getProperty("user.dir") + "\\config\\ActionConfig.txt");
	}

}