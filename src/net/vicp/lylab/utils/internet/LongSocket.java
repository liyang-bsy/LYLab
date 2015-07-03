package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.TranscodeObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.HeartBeatSender;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;

/**
 * Long socket can communicate with server multiple times without close socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class LongSocket extends LYSocket implements HeartBeatSender {
	private static final long serialVersionUID = -4542553667467771646L;
	protected SequenceTemporaryPool<TranscodeObject> dataPool = new SequenceTemporaryPool<TranscodeObject>();
	protected HeartBeat heartBeat;

	public LongSocket(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket);
		this.heartBeat = heartBeat;
	}

	public LongSocket(String host, Integer port, HeartBeat heartBeat) {
		super(host, port);
		this.heartBeat = heartBeat;
	}

	@Override
	protected boolean isDaemon() {
		return true;
	}

	@Override
	public void exec() {
		try {
			connect();
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
	}

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
			TranscodeObject tmp = dataPool.accessOne();
			if (tmp == null) {
				await(CoreDef.WAITING_LONG);
				if(!sendHeartBeat(heartBeat)) return null;
				continue;
			}
			signalAll();
			result = request(tmp.encode().toBytes());
			if (result == null)
				dataPool.add(0, tmp);
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
	public Long addToPool(TranscodeObject data) {
		Long objId = dataPool.add(data);
		if (objId != null) {
			signalAll();
		}
		return objId;
	}

	/**
	 * Add to pool by force, keep wait until added
	 * 
	 * @param data
	 * @return ObjectId of data, it won't return until data was added
	 */
	public Long addToPool_Force(TranscodeObject data) {
		Long objId = null;
		while (((objId = dataPool.add(data)) == null))
			await(CoreDef.WAITING_LONG);
		if (objId != null) {
			signalAll();
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
