package net.vicp.lylab.utils.internet.async_test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.controller.TimeoutController;

/**
 * Can be used in aop to request a sync request
 * @author Young
 *
 */
public class Transfer extends NonCloneableBaseObject implements Recyclable {
	protected Map<String, Message> responseMap = new ConcurrentHashMap<String, Message>();
	protected AtomicBoolean isClosed = new AtomicBoolean(false);
	protected long timeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;
	protected long readTimeout = CoreDef.DEFAULT_READ_TTIMEOUT;
	protected Aop aopLogic;
	
	public Transfer(Aop aopLogic) {
		TimeoutController.addToWatch(this);
		this.aopLogic = aopLogic;
	}

	public void sendRequest(Message request) {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		request.setUuid(uuid);
	}

	public Message recvResponse(String msgId) throws InterruptedException {
		return recvResponse(msgId, CoreDef.WAITING_SHORT);
	}

	public Message recvResponse(String msgId, long millis) throws InterruptedException {
		if(isClosed()) throw new LYException("This transport pool is closed");
		Message response = null;
		int attempts = 0;
		while((response = responseMap.remove(msgId)) == null)
		{
			if(attempts*millis > CoreDef.REQUEST_TTIMEOUT)
				return null;
			attempts ++;
			Thread.sleep(millis);
		}
		return response;
	}

	public void received(Message request) {
		if(isClosed()) throw new LYException("This transport pool is closed");
		responseMap.put(request.getUuid(), request);
	}
	
	@Override
	public void close() {
		if(isClosed.getAndSet(true)) return;
		TimeoutController.removeFromWatch(this);
		responseMap = new ConcurrentHashMap<String, Message>();
	}

	@Override
	public boolean isRecyclable() {
		return !responseMap.isEmpty();
	}

	@Override
	public void recycle() {
		recycle(1.0);
	}
	
	public void recycle(double rate) {
		synchronized (lock) {
			for (String msgId : responseMap.keySet()) {
				Message msg = responseMap.get(msgId);
				if (System.currentTimeMillis() - msg.getTime() > timeout * rate) {
					responseMap.remove(msgId);
				}
			}
		}
	}

	public int size() {
		return responseMap.size();
	}

	public boolean isEmpty() {
		return responseMap.isEmpty();
	}

	public void clear() {
		if(isClosed()) return;
		responseMap.clear();
	}

	public boolean isClosed() {
		return isClosed.get();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

}
