package net.vicp.lylab.utils.internet;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.ObjectContainer;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.creator.SelectorCreator;
import net.vicp.lylab.utils.internet.transfer.Transfer;

/**
 * A async socket can be used for communicating with paired client.
 * This task socket should be used on LYTaskQueue
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.21
 * @version 0.0.5
 */
public class AsyncSocket extends BaseSocket implements LifeCycle {//, Transmission {
	private static final long serialVersionUID = -3262692917974231303L;
	
	// Raw data source
	protected Selector selector = null;
	protected SelectionKey selectionKey = null;
	// Not null unless isServer() == false
	protected SocketChannel socketChannel = null;
	
	// TODO will be useful on push data to client
	// Clients			ip	 local port		Socket
	protected Map<Pair<String, Integer>, SocketChannel> ip2client = new HashMap<>();
	protected AutoGeneratePool<ObjectContainer<Selector>> selectorPool;
	protected Transfer transfer;

	// Long socket keep alive
	protected Map<String, Long> lastActivityMap = new ConcurrentHashMap<String, Long>();
	protected HeartBeat heartBeat;
	protected long interval = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT/10;

	// Buffer
	private ByteBuffer niobuf = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
//	private byte[] buffer = new byte[CoreDef.SOCKET_MAX_BUFFER];
	private int maxBufferSize = CoreDef.SOCKET_MAX_BUFFER;
	
	/**
	 * <b>[Server mode]</b><br>
	 * Async is only useful on Long Socket
	 * 
	 * @param port
	 * @param heartBeat
	 */
	public AsyncSocket(int port, Transfer transfer, HeartBeat heartBeat) {
		super.setLonewolf(true);
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			transfer.setAsyncSocket(this);
			this.transfer = transfer;
			this.heartBeat = heartBeat;
			setServer(true);
		} catch (Exception e) {
			throw new LYException("Establish server failed", e);
		}
	}
	
	/**
	 * Client mode
	 * @param port
	 */
	public AsyncSocket(String host, int port, HeartBeat heartBeat) {
		throw new LYException("Async Socket is only available for Server");
//		try {
//			selector = Selector.open();
//			socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
//			socketChannel.configureBlocking(false);
//			socketChannel.register(selector, SelectionKey.OP_READ);
//			this.heartBeat = heartBeat;
//			setServer(false);
//		} catch (Exception e) {
//			throw new LYException("Connect to server failed", e);
//		}
	}

	public void selectionKeyHandler()
	{
		if (selectionKey.isAcceptable()) {
			try {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
				SocketChannel socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				Socket socket = socketChannel.socket();
				ip2client.put(getClientSession(socket), socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				throw new LYException("Close failed", e);
			}
		} else if (selectionKey.isReadable()) {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			try {
//				receiveBasedAopDrive(socketChannel);
				Pair<byte[], Integer> receivedData = receive(socketChannel);
				transfer.putRequest(socketChannel, receivedData);
			} catch (Throwable t) {
				if (socketChannel != null) {
					try {
						socketChannel.close();
					} catch (Exception ex) {
						log.error("Close failed"
								+ Utils.getStringFromException(ex));
					}
					socketChannel = null;
				}
				log.error(Utils.getStringFromThrowable(t));
			}
		} else if (selectionKey.isWritable()) {
			System.out.println("TODO: isWritable()");
		} else if (selectionKey.isConnectable()) {
			System.out.println("TODO: isConnectable()");
		} else {
			System.out.println("TODO: else");
		}

	}

	@Override
	public void exec() {
		try {
			// Will be block here
			while (!isStopped() && selector.select() > 0) {
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					selectionKey = iterator.next();
					iterator.remove();
					selectionKeyHandler();
				}
			}
		} catch (Exception e) {
			throw new LYException("Connect break", e);
		} finally {
			try {
				close();
			} catch (Exception ex) {
				log.info(Utils.getStringFromException(ex));
			}
		}
	}

	public Pair<byte[], Integer> receive(SocketChannel socketChannel) {
		if (isClosed())
			throw new LYException("Connection closed");
		if (socketChannel == null)
			throw new NullPointerException("Parameter socketChannel is null");
		byte[] buffer = new byte[maxBufferSize];
		int bufferLen = 0;
		int ret = 0;
		niobuf.clear();
		while (true) {
			try {
				ret = socketChannel.read(niobuf);
				if (ret <= 0) {
					if (ret == 0) {
						// move niobuf to buffer
						Utils.bytecat(buffer, bufferLen, niobuf.array(), 0, niobuf.position());
						bufferLen += niobuf.position();
						break;
					} else if (ret == -1)
						return null;
					else
						throw new LYException("IMPOSSIBLE?");
				}
				if (niobuf.remaining() == 0) {
					// extend current max size
					maxBufferSize *= CoreDef.SOCKET_MAX_BUFFER_EXTEND_RATE;
					buffer = Arrays.copyOf(buffer, maxBufferSize);
					// move niobuf to buffer
					Utils.bytecat(buffer, bufferLen, niobuf.array(), 0, niobuf.position());
					bufferLen += niobuf.position();
					niobuf = ByteBuffer.allocate(maxBufferSize);
				}
			} catch (Exception e) {
				throw new LYException("Socket read failed", e);
			}
		}
		return new Pair<>(buffer, bufferLen);
	}
	
	public boolean send(SocketChannel socketChannel, byte[] request) {
		try {
			if (isClosed())
				return false;
			if (socketChannel != null && flushChannel(socketChannel, ByteBuffer.wrap(request), CoreDef.DEFAULT_SOCKET_WRITE_TTIMEOUT)>0)
				return true;
			return false;
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	private int flushChannel(SocketChannel socketChannel, ByteBuffer bb, long writeTimeout) throws Exception {
		SelectionKey key = null;
		Selector writeSelector = null;
		int torelent = 0;
		int bytesProduced = 0;
		try {
			while (bb.hasRemaining()) {
				int len = socketChannel.write(bb);
				torelent++;
				bytesProduced += len;
				if (len == 0) {
	                if (writeSelector == null){
						writeSelector = selectorPool.accessOne().getObject();
	                    if (writeSelector == null){
	                        // Continue using the main one
	                        continue;
	                    }
	                }
					key = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
					if (writeSelector.select(writeTimeout) == 0) {
						if (torelent > 5) {
							try {
								socketChannel.close();
							} catch (Exception e) {
								throw new LYException("Lost connection to client, and close socket channel failed", e);
							}
							throw new LYException("Lost connection to client");
						}
					} else {
						torelent--;
					}
				} else {
					torelent = 0;
				}
			}
		} finally {
			if (key != null) {
				key.cancel();
				key = null;
			}
			if (writeSelector != null) {
				// Cancel the key.
				writeSelector.selectNow();
				selectorPool.recycle(ObjectContainer.fromObject(writeSelector));
			}
		}
		return bytesProduced;
	}

	@Override
	public void initialize() {
		if (isServer()) {
			SelectorCreator creator = new SelectorCreator();
			selectorPool = new AutoGeneratePool<ObjectContainer<Selector>>(creator, null,
					CoreDef.DEFAULT_CONTAINER_TIMEOUT, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
			begin("AsyncServer");
		}
		transfer.initialize();
	}
	
	@Override
	public void close() {
		try {
			if (isClosed()) return;
			for (Pair<String, Integer> addr : ip2client.keySet()) {
				try {
					SocketChannel socketChannel = ip2client.get(addr);
					socketChannel.socket().close();
				} catch (Exception e) {
					log.debug("Close failed, maybe client already lost connection" + Utils.getStringFromException(e));
				}
			}
			if (transfer != null) {
				transfer.close();
				transfer = null;
			}
			if (selector != null) {
				selector.close();
				selector = null;
			}
			if (thread != null)
				callStop();
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return !selector.isOpen();
	}

	// TODO will be useful in client mode
	// Keep alive
//	@Override
//	public void setInterval(long interval) {
//		this.interval = interval;
//	}
//
//	@Override
//	public boolean isOutdated() {
//		long earliest = Long.MAX_VALUE;
//		for (String key : lastActivityMap.keySet()) {
//			long tmp = lastActivityMap.get(key);
//			if (tmp < earliest)
//				earliest = tmp;
//		}
//		if(System.currentTimeMillis() - earliest > interval)
//			return true;
//		return false;
//	}
//
//	@Override
//	public boolean keepAlive() {
//		if(!isOutdated()) return true;
//		try {
//			if(protocol == null)
//				return true;
//			List<String> keepAliveList = new ArrayList<String>();
//			for (String key : lastActivityMap.keySet()) {
//				long lastActivity = lastActivityMap.get(key);
//				if (System.currentTimeMillis() - lastActivity > interval) {
//					keepAliveList.add(key);
//				}
//			}
//			for(String ip:keepAliveList)
//				try {
//					byte[] bytes = request(ip, protocol.encode(heartBeat));
//					if (bytes != null) {
//						Object obj = protocol.decode(bytes);
//						if (obj instanceof HeartBeat)
//							return true;
//						else
//							log.error("Send heartbeat failed\n" + obj.toString());
//					}
//				} catch (Exception e) {
//					SocketChannel socketChannel = ipMap.get(ip);
//					try {
//						socketChannel.close();
//					} catch (Exception ex) {
//						log.error(Utils.getStringFromException(ex));
//					}
//				}
//			return true;
//		} catch (Exception e) {
//			log.error("This socket may be dead" + Utils.getStringFromException(e));
//		}
//		return false;
//	}
//
//	@Override
//	public boolean isAlive() {
//		if (isClosed())
//			return false;
//		if(!keepAlive()) return false;
//		return true;
//	}

}
