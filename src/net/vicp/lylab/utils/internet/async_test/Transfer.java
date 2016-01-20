package net.vicp.lylab.utils.internet.async_test;

import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.controller.TimeoutController;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * Can be used in aop to request a sync request
 * @author Young
 *
 */
public class Transfer extends LoneWolf {//implements Recyclable {
	//					nio client		Data
	protected SequenceTemporaryPool<Pair<SocketChannel, byte[]>> requestPool = new SequenceTemporaryPool<>();
	//
//	protected List<Pair<Channel, byte[]>> responses = new ArrayList<>();
//	protected long timeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;
	protected AtomicBoolean closed = new AtomicBoolean(false);
//	protected long readTimeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;
	protected AsyncSocket asyncSocket;
	protected Aop aopLogic;
	
	public Transfer(AsyncSocket asyncSocket, Aop aopLogic) {
//		TimeoutController.addToWatch(this);
		this.aopLogic = aopLogic;
		this.asyncSocket = asyncSocket;
	}

	public void putRequest(SocketChannel socketChannel, byte[] request) {
		requestPool.add(new Pair<>(socketChannel, request));
		signalAll();
	}

	public void putResponse(SocketChannel socketChannel, byte[] response) {
		asyncSocket.send(socketChannel, response);
	}

	@Override
	public void exec() {
		while (true) {
			if (requestPool.isEmpty()) {
				await(100);
				continue;
			}
			Pair<SocketChannel, byte[]> pair = requestPool.accessOne();
			SocketChannel socketChannel = pair.getLeft();
			byte[] request = pair.getRight();
			byte[] response = aopLogic.doAction(socketChannel .socket(), request, 0);
			asyncSocket.send(socketChannel, response);
		}
	}

//	public Message recvResponse(String msgId, long millis) throws InterruptedException {
//		if(isClosed()) throw new LYException("This transport pool is closed");
//		Message response = null;
//		int attempts = 0;
//		while((response = responseMap.remove(msgId)) == null)
//		{
//			if(attempts*millis > CoreDef.REQUEST_TTIMEOUT)
//				return null;
//			attempts ++;
//			Thread.sleep(millis);
//		}
//		return response;
//	}

//	public void received(Message request) {
//		if(isClosed()) throw new LYException("This transport pool is closed");
//		responseMap.put(request.getUuid(), request);
//	}
	
//	@Override
//	public void close() {
//		if(closed.getAndSet(true)) return;
//		TimeoutController.removeFromWatch(this);
//		requests.clear();
//		responses.clear();
//	}
//
//	@Override
//	public boolean isRecyclable() {
//		return !responses.isEmpty();
//	}
//
//	@Override
//	public void recycle() {
//		recycle(1.0);
//	}
//	
//	public void recycle(double rate) {
//		synchronized (lock) {
//			for (String msgId : responses.keySet()) {
//				Message msg = responses.get(msgId);
//				if (System.currentTimeMillis() - msg.getTime() > timeout * rate) {
//					responses.remove(msgId);
//				}
//			}
//		}
//	}
//
//	public int size() {
//		return responseMap.size();
//	}
//
//	public boolean isEmpty() {
//		return responseMap.isEmpty();
//	}
//
//	public void clear() {
//		if(isClosed()) return;
//		responseMap.clear();
//	}
//
//	public boolean isClosed() {
//		return isClosed.get();
//	}
//
//	public long getTimeout() {
//		return timeout;
//	}
//
//	public void setTimeout(long timeout) {
//		this.timeout = timeout;
//	}
//
//	public long getReadTimeout() {
//		return readTimeout;
//	}
//
//	public void setReadTimeout(long readTimeout) {
//		this.readTimeout = readTimeout;
//	}

}
