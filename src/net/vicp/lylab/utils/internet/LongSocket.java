package net.vicp.lylab.utils.internet;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.pool.SequenceTemporaryPool;
import net.vicp.lylab.utils.Utils;

/**
 * Long socket can communicate with server multiple times without close socket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class LongSocket extends LYSocket {
	private static final long serialVersionUID = -4542553667467771646L;
	protected SequenceTemporaryPool<byte[]> dataPool = new SequenceTemporaryPool<byte[]>();
	protected HeartBeat heartBeat;

	public LongSocket(ServerSocket serverSocket, HeartBeat heartBeat) {
		super(serverSocket);
		this.heartBeat = heartBeat;
	}

	public LongSocket(String host, Integer port, Protocol protocol, HeartBeat heartBeat) {
		super(host, port);
		this.heartBeat = heartBeat;
		this.protocol = protocol;
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
				send("Connection break".getBytes());
			} catch (Exception ex) {
				log.info(Utils.getStringFromException(ex));
			}
			try {
				close();
			} catch (Exception ex) {
				log.info(Utils.getStringFromException(ex));
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
			log.error(Utils.getStringFromException(e));
		}
		return ret;
	}

	@Override
	public byte[] doRequest(byte[] request) {
		if (isServer())
			throw new LYException("Do request is forbidden to a server socket");
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		byte[] result = null;
		try {
			do {
				byte[] tmp = dataPool.accessOne();
				if (tmp == null) {
					if(!sendHeartBeat()) return null;
					await(CoreDef.WAITING_LONG);
					continue;
				}
				signalAll();
				result = request(tmp);
				if (result == null)
					dataPool.add(0, tmp);
				break;
			} while (true);
			if(afterTransmission != null)
				afterTransmission.callback(result);
		} catch (Exception e) { }
		return result;
	}

	/**
	 * Add to pool
	 * 
	 * @param data
	 * @return ObjectId of data, null if add failed
	 */
	public Long addToPool(byte[] data) {
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
	public Long addToPool_Force(Object data) {
		Long objId = null;
		byte[] dateBytes = protocol.encode(data);
		while (((objId = dataPool.add(dateBytes)) == null))
			await(CoreDef.WAITING_LONG);
		if (objId != null) {
			signalAll();
		}
		return objId;
	}

	public boolean sendHeartBeat() {
		try {
			if(protocol == null)
				return true;
			if(request(protocol.encode(heartBeat)) != null)
				return true;
		} catch (Exception e) { }
		return false;
	}

}
