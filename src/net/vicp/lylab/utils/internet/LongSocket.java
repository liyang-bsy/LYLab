package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

import net.vicp.lylab.core.TranscodeProtocol;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;

public class LongSocket<T> extends LYSocket implements HeartBeatSender {
	private static final long serialVersionUID = -4542553667467771646L;
	protected SequenceTemporaryPool<T> dataPool = new SequenceTemporaryPool<T>();

	public LongSocket(ServerSocket serverSocket) {
		super(serverSocket);
	}

	public LongSocket(String host, int port) {
		super(host, port);
	}

	@Override
	protected boolean isDaemon() {
		return true;
	}

	@Override
	public void exec() {
		connect();
		try {
			if (isServer()) {
				while (true)
				{
					byte[] bytes = receive();
					if(bytes == null) return;
					send(doResponse(bytes));
				}
			} else {
				while (doRequest(null) != null);
			}
		} catch (Exception e) {
			throw new LYException("Connect break", e);
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new LYException("Why?", e);
			}
		}
	}
	
	@Override
	protected void aftermath() {
		log.info(isServer()?"Lost connection to client":"Lost connection to server");
	};

	@Override
	public byte[] request(byte[] request) {
		if (isServer())
			return null;
		byte[] ret = null;
		try {
			send(request);
			ret = receive();
		} catch (Exception e) {
			try {
				recycle();
			} catch (Throwable t) {
				return null;
			}
		}
		return ret;
	}

	@Override
	public byte[] doRequest(byte[] request) {
		if (isServer())
			throw new LYException("Do request is forbidden to a server socket");
		byte[] result = null;
		do {
			T tmp = dataPool.accessOne();
			if (tmp == null) {
				waitCycle();
				if(!sendHeartBeat(new HeartBeat())) return null;
				continue;
			}
			result = request(((TranscodeProtocol) tmp).encode().toBytes());
			if (result == null) dataPool.add(0, tmp);
			System.out.println(Protocol.fromBytes(result));
			break;
		} while (true);
		return result;
	}

	/**
	 * Add to pool
	 * 
	 * @param data
	 * @return ObjectId of data, null if add failed
	 */
	public Long addToPool(T data) {
		Long objId = dataPool.add(data);
		if (objId != null) {
			interrupt();
		}
		return objId;
	}

	@Override
	public boolean sendHeartBeat(HeartBeat heartBeat) {
		try {
			if(request(heartBeat.encode().toBytes()) != null)
				return true;
		} catch (Exception e) { }
		return false;
	}

}
