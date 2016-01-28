package net.vicp.lylab.utils.internet.transfer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.DispatchExecutor;
import net.vicp.lylab.utils.tq.LYTaskQueue;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public class AsyncTransfer extends AbstractTransfer {
	private static final long serialVersionUID = -8449620508452125989L;

	public AsyncTransfer(Session session, Protocol protocol, LYTaskQueue taskQueue,
			Dispatcher<? super Object, ? super Object> dispatcher) {
		super(session, protocol, taskQueue, dispatcher);
	}

	@Override
	public void initialize() {
		if(protocol == null)
			throw new LYException("No protocol is assigned");
		if(taskQueue == null)
			throw new LYException("No taskQueue is assigned");
		if (session == null)
			throw new LYException("No session is assigned");
		if (!closed.compareAndSet(true, false))
			return;
		this.begin("Async Transfer");
		super.initialize();
	}

	private boolean validateRequest() {
		boolean noMoreRequest = true;
		if (!addr2validate.isEmpty()) {
			synchronized (lock) {
				Iterator<Entry<InetAddr, Boolean>> addrContainerIterator = addr2validate.entrySet().iterator();
				while (addrContainerIterator.hasNext()) {
					Entry<InetAddr, Boolean> addrContainer = addrContainerIterator.next();
					addrContainerIterator.remove();
					if (!addrContainer.getValue())
						continue;
					try {
						Pair<byte[], Integer> byteContainer = addr2byte.get(addrContainer.getKey());
						synchronized (byteContainer) {
							int start = 0, next = 0;
							boolean newReuqest = false;
							while (true) {
								start = next;
								if ((next = protocol.validate(byteContainer.getLeft(), start,
										byteContainer.getRight())) == 0)
									break;
								if (next <= byteContainer.getRight()) {
									byte[] fullReq = new byte[next - start];
									Utils.bytecat(fullReq, 0, byteContainer.getLeft(), start, next - start);
									requestPool.add(new Pair<>(addrContainer.getKey(), fullReq));
									newReuqest = true;
								}
							}
							if (newReuqest) {
								Utils.bytecat(byteContainer.getLeft(), 0, byteContainer.getLeft(), start,
										byteContainer.getRight() - start);
								byteContainer.setRight(byteContainer.getRight() - start);
								Arrays.fill(byteContainer.getLeft(), byteContainer.getRight(),
										byteContainer.getLeft().length, (byte) 0);
								noMoreRequest = false;
							}
						}
					} catch (Exception e) {
						log.error("Decoding requests failed:" + Utils.getStringFromException(e));
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
				await(CoreDef.WAITING_LONG);
			else {
				Pair<InetAddr, byte[]> request = requestPool.accessOne();
				taskQueue.addTask(new DispatchExecutor<>(session.getClient(request.getLeft()), request.getRight(),
						session, dispatcher, protocol));
			}
		}
	}

}
