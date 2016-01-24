package net.vicp.lylab.utils.internet.transfer;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.controller.TimeoutController;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.internet.Session;
import net.vicp.lylab.utils.tq.LYTaskQueue;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public class SyncTransfer extends AbstractTransfer {
	private static final long serialVersionUID = 1706498472211433734L;
	
	//					ip		port	validate
	protected Map<Pair<String, Integer>, Boolean> addr2validate = new HashMap<>();
	//					ip		port	startTime
	protected Map<Pair<String, Integer>, Long> addr2timeout = new HashMap<>();
	//					ip		port			client				byte buffer
	protected Map<Pair<String, Integer>, Pair<SocketChannel, Pair<byte[], Integer>>> addr2byte = new HashMap<>();
	//									nio client		Data
	protected SequenceTemporaryPool<Pair<SocketChannel, byte[]>> requestPool = new SequenceTemporaryPool<>();
	//
	protected long timeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected Session session;
	protected Protocol protocol;
	protected LYTaskQueue taskQueue;
	protected AutoCreator<Dispatcher<?, ?>> dispatcherCreator;

	@Override
	public void initialize() {
		if(protocol == null)
			throw new LYException("No protocol is assigned");
		if(taskQueue == null)
			throw new LYException("No taskQueue is assigned");
		if(session == null)
			throw new LYException("No asyncSocket is assigned");
		if (!closed.compareAndSet(true, false))
			return;
		TimeoutController.addToWatch(this);
		this.begin("transfer");
	}

	public void putRequest(SocketChannel socketChannel, Pair<byte[], Integer> receivedData) {
		Socket socket = socketChannel.socket();
		Pair<String, Integer> addr = new Pair<>(socket.getInetAddress().getHostAddress(), socket.getPort());
		if (!addr2byte.containsKey(addr))
			addr2byte.put(addr, new Pair<>(socketChannel, receivedData));
		else {
			Pair<SocketChannel, Pair<byte[], Integer>> container = addr2byte.get(addr);
			synchronized (container) {
				if (container.getLeft().equals(socketChannel)) {
					container.getRight().setLeft(Utils.bytecat(container.getRight().getLeft(), container.getRight().getRight(),
							receivedData.getLeft(), 0, receivedData.getRight()));
					container.getRight().setRight(container.getRight().getRight() + receivedData.getRight());
				} else
					throw new LYException("Socket Chennel no matches");
			}
		}
		addr2validate.put(addr, true);
		addr2timeout.put(addr, System.currentTimeMillis());
		//requestPool.add(new Pair<>(socketChannel, request));
		//signalAll();
	}

	public void putResponse(Socket socket, byte[] response) {
		session.send(socket, response);
	}

	private boolean validateRequest() {
		boolean noMoreRequest = true;
		if (!addr2validate.isEmpty()) {
			Iterator<Entry<Pair<String, Integer>, Boolean>> addrContainerIterator = addr2validate.entrySet().iterator();
			while (addrContainerIterator.hasNext()) {
				Entry<Pair<String, Integer>, Boolean> addrContainer = addrContainerIterator.next();
				addrContainerIterator.remove();
				if (!addrContainer.getValue())
					continue;

				Pair<SocketChannel, Pair<byte[], Integer>> clientContainer = addr2byte.get(addrContainer.getKey());

				synchronized (clientContainer) {
					Pair<byte[], Integer> byteContainer = clientContainer.getRight();

					int start = 0, next = 0;
					boolean newReuqest = false;
					while (true) {
						start = next;
						if ((next = protocol.validate(byteContainer.getLeft(), start, byteContainer.getRight())) == 0)
							break;
						if (next <= byteContainer.getRight()) {
							byte[] fullReq = new byte[next - start];
							Utils.bytecat(fullReq, 0, byteContainer.getLeft(), start, next - start);
							requestPool.add(new Pair<>(clientContainer.getLeft(), fullReq));
							newReuqest = true;
						}
					}
					if (newReuqest) {
						Utils.bytecat(byteContainer.getLeft(), 0, byteContainer.getLeft(), start,
								byteContainer.getRight() - start);
						byteContainer.setRight(byteContainer.getRight() - start);
						Arrays.fill(byteContainer.getLeft(), byteContainer.getRight(), byteContainer.getLeft().length,
								(byte) 0);
						noMoreRequest = false;
					}
				}
			}
		}
		return noMoreRequest;
	}
	
	@Override
	public void exec() {
		while (true) {
			if (requestPool.isEmpty() && validateRequest())
				await(1000);
			else
			{
				Pair<SocketChannel, byte[]> request = requestPool.accessOne();
				taskQueue.addTask(new DispatchExecutor<>(
						request.getLeft().socket(), request.getRight(), session, dispatcherCreator.newInstance(), protocol));
			}
//			Pair<SocketChannel, byte[]> pair = requestPool.accessOne();
//			SocketChannel socketChannel = pair.getLeft();
//			byte[] request = pair.getRight();
//			byte[] response = null;
//			Aop aop = asyncSocket.getAopLogic();
//			if (aop != null)
//				response = aop.doAction(socketChannel.socket(), request, 0);
//			else
//				response = request;
//			asyncSocket.send(socketChannel, response);
		}
	}

	@Override
	public void close() {
		if (closed.getAndSet(true))
			return;
		TimeoutController.removeFromWatch(this);
		addr2validate.clear();
		addr2timeout.clear();
		addr2byte.clear();
		requestPool.clear();
	}

	@Override
	public boolean isRecyclable() {
		return !addr2timeout.isEmpty();
	}

	@Override
	public void recycle() {
		recycle(1.0);
	}
	
	public void recycle(double rate) {
		synchronized (lock) {
			Iterator<Entry<Pair<String, Integer>, Long>> addrIterator = addr2timeout.entrySet().iterator();
			while (addrIterator.hasNext()) {
				Entry<Pair<String, Integer>, Long> entry = addrIterator.next();
				if (System.currentTimeMillis() - entry.getValue() > timeout * rate) {
					addr2validate.remove(entry.getKey());
					addr2byte.remove(entry.getKey());
					addrIterator.remove();
				}
			}
		}
	}

	public int validate(byte[] bytes, int len) {
		return protocol.validate(bytes, len);
	}

	public int validate(byte[] bytes, int offset, int len) {
		return protocol.validate(bytes, offset, len);
	}

	// getters & setters

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public LYTaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LYTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

}
