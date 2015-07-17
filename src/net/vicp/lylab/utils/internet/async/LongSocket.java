package net.vicp.lylab.utils.internet.async;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.KeepAlive;
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
public class LongSocket extends AsyncServer implements KeepAlive {
	private static final long serialVersionUID = -4542553667467771646L;
	protected SequenceTemporaryPool<byte[]> dataPool = new SequenceTemporaryPool<byte[]>();
	protected HeartBeat heartBeat;
	protected long lastActivity = 0L;
	protected long interval = CoreDef.DEFAULT_SOCKET_TTIMEOUT/4;

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
		String lastWord = "Connect break";
		try {
			if (isServer()) {
				while (true) {
					byte[] bytes = receive();
					if (bytes == null)
						return;
					bytes = doResponse(bytes);
					if (bytes == null) {
						throw new LYException("Server attempt response null to client");
					}
					send(bytes);
				}
			} else {
				connect();
				while (doRequest(null) != null);
			}
		} catch (Throwable t) {
			lastWord = t.getMessage();
			throw new LYException("Connect break", t);
		} finally {
			try {
				send(lastWord.getBytes());
			} catch (Exception e) {
				log.info(Utils.getStringFromException(e));
			}
			try {
				close();
			} catch (Exception e) {
				log.info(Utils.getStringFromException(e));
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
		synchronized(lock)
		{
			lastActivity = System.currentTimeMillis();
			byte[] ret = null;
			try {
				send(request);
				ret = receive();
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
			return ret;
		}
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
					if(!keepAlive()) return null;
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
		} catch (Exception e) {
			throw new LYException(e);
		}
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

	public int getDataPoolSize() {
		return dataPool.size();
	}

	@Override
	public void start() {
		connect();
	}

	@Override
	public void setInterval(long interval) {
		this.interval = interval;
	}

	@Override
	public boolean isDying() {
		synchronized (lock) {
			if(System.currentTimeMillis() - lastActivity > interval)
				return true;
			return false;
		}
	}

	@Override
	public boolean keepAlive() {
		synchronized (lock) {
			if(!isDying()) return true;
			try {
				if(protocol == null)
					return true;
				byte[] bytes = request(protocol.encode(heartBeat));
				if (bytes != null) {
					Object obj = protocol.decode(bytes);
					if (obj instanceof HeartBeat)
						return true;
					else
						log.error("Send heartbeat failed\n" + obj.toString());
				}
			} catch (Exception e) { }
			return false;
		}
	}

	@Override
	public boolean isAlive() {
		if (isClosed())
			return false;
		// It is said that this method may cause unexpected result on Windows 7
		/*try {
			socket.sendUrgentData(0xFF);
		} catch (Exception e) {
			return false;
		}*/
		if(!keepAlive()) return false;
		return true;
	}

}
