package net.vicp.lylab.server.aop;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.AbstractAction;
import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.SimpleMessage;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;

public abstract class AbstractDispatcherAop<I extends O, O extends SimpleMessage> extends NonCloneableBaseObject implements Aop {
	protected List<Filter<I, O>> filterChain = new ArrayList<Filter<I, O>>();
	protected Protocol protocol = null;

	protected abstract void dispatcher(AbstractAction action, Socket client, I request, O response);

	// Java language defect
	protected abstract O newResponse();

	/**
	 * 
	 * @param request
	 * @return null means no action was mapped to current request
	 */
	protected abstract AbstractAction mapAction(I request);

	protected void logger(I request, O response) {
	}

	@Override
	public void initialize() {
		if (protocol == null)
			throw new LYException("No available protocol");
	}

	@Override
	public void close() throws Exception {
		for (Filter<I, O> filter : filterChain)
			filter.close();
	}

	protected O filterChain(Socket client, I request) {
		// do start filter
		if (filterChain != null && filterChain.size() != 0)
			for (Filter<I, O> filter : filterChain) {
				O ret = null;
				if ((ret = filter.doFilter(client, request)) != null)
					return ret;
			}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] doAction(Socket client, byte[] requestByte, int offset) {
		I request = null;

		BaseAction action = null;
		O response = newResponse();
		try {
			do {
				// decode
				try {
					Object obj = protocol.decode(requestByte, offset);
					if(obj instanceof HeartBeat)
						return protocol.encode(obj);
					request = (I) obj;
				} catch (Exception e) {
					log.debug(Utils.getStringFromException(e));
					response.setCode(0x00000001);
					response.setMessage("Bad formation, decode failed");
					break;
				}
				// decode nothing
				if (request == null) {
					response.setCode(0x00000002);
					response.setMessage("Message not found");
					break;
				}
				// filter chain
				try {
					O tmp = filterChain(client, request);
					if (tmp != null) {
						response = tmp;
						break;
					}
				} catch (Exception e) {
					response.setCode(0x00000003);
					response.setMessage("Filter chain runtime error");
					break;
				}
				// sync response and request
				response.copyBasicInfo(request);

				try {
					dispatcher(action, client, request, response);
				} catch (Throwable t) {
					String reason = Utils.getStringFromThrowable(t);
					log.error(reason);
					response.setCode(0x00000004);
					response.setMessage("Action dispatch/execute failed:" + reason);
				}
			} while (false);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		try {
			// save access log
			logger(request, response);
		} catch (Exception e) {
			log.error("Logger failed:" + Utils.getStringFromException(e));
		}
		return protocol.encode(response);
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public Aop setProtocol(Protocol protocol) {
		this.protocol = protocol;
		return this;
	}

	public List<Filter<I, O>> getFilterChain() {
		return filterChain;
	}

	public void setFilterChain(List<Filter<I, O>> filterChain) {
		this.filterChain = filterChain;
	}

}