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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.interfaces.Transfer;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.core.model.ObjectContainer;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.controller.TimeoutController;
import net.vicp.lylab.utils.creator.SelectorCreator;
import net.vicp.lylab.utils.internet.transfer.PooledAsyncTransfer;
import net.vicp.lylab.utils.tq.LYTaskQueue;

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
public class AsyncSession extends AbstractSession implements LifeCycle, Recyclable {//, Transmission {
	private static final long serialVersionUID = -3262692917974231303L;
	
	// Raw data source
	protected Selector selector = null;
	
	// Clients			client			Socket
	protected Map<InetAddr, SocketChannel> addr2client = new ConcurrentHashMap<>();
	protected AutoGeneratePool<ObjectContainer<Selector>> selectorPool;
	protected Transfer transfer;

	// Long socket keep alive & recycle bad request
	protected Map<InetAddr, Long> lastActivityMap = new ConcurrentHashMap<InetAddr, Long>();
	protected HeartBeat heartBeat;
	protected long interval = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT/10;
	long timeout = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT;

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
	public AsyncSession(int port, Protocol protocol, Dispatcher<? super Confirm, ? super Confirm> dispatcher,
			HeartBeat heartBeat, LYTaskQueue taskqueue, int maxHandlerSize) {
		super(protocol, dispatcher, heartBeat);
		super.setLonewolf(true);
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			transfer = new PooledAsyncTransfer(this, protocol, taskqueue, dispatcher, maxHandlerSize);
			this.heartBeat = heartBeat;
			setServer(true);
			TimeoutController.addToWatch(this);
		} catch (Exception e) {
			throw new LYException("Establish server failed", e);
		}
	}
	
	/**
	 * Client mode
	 * @param port
	 */
	public AsyncSession(String host, Integer port, Protocol protocol, HeartBeat heartBeat) {
		super(protocol, null, heartBeat);
		throw new LYException("Async Socket is only available for Server");
//		try {
//			selector = Selector.open();
//			socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
//			socketChannel.configureBlocking(false);
//			socketChannel.register(selector, SelectionKey.OP_READ);
//			this.heartBeat = heartBeat;
//			setServer(false);
//			TimeoutController.addToWatch(this);
//		} catch (Exception e) {
//			throw new LYException("Connect to server failed", e);
//		}
	}

	public void selectionKeyHandler(SelectionKey selectionKey)
	{
		SocketChannel socketChannel = null;
		if (selectionKey.isAcceptable()) {
			try {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
				socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				addr2client.put(Utils.getPeer(socketChannel), socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				throw new LYException("Close failed", e);
			}
		} else if (selectionKey.isReadable()) {
			try {
				socketChannel = (SocketChannel) selectionKey.channel();
				Pair<byte[], Integer> data = receive(socketChannel.socket());
				if (data == null) {
					selectionKey.cancel();
					socketChannel.close();
				}
				else
					transfer.putRequest(Utils.getPeer(socketChannel), data.getLeft(), data.getRight());
			} catch (Throwable t) {
				if (socketChannel != null) {
					try {
						socketChannel.close();
					} catch (Exception ex) {
						log.error("Close failed" + Utils.getStringFromException(ex));
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
		// record last activate
		if (socketChannel != null)	// won't be true
			lastActivityMap.put(Utils.getPeer(socketChannel), System.currentTimeMillis());

	}

	@Override
	public void exec() {
		try {
			// Will be block here
			while (!isStopped()) {
				selector.select();
				synchronized (lock) {
					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while (iterator.hasNext()) {
						SelectionKey selectionKey = iterator.next();
						iterator.remove();
						selectionKeyHandler(selectionKey);
					}
				}
			}
		} catch (Throwable t) {
			Utils.printStack("AsyncSession is stopped" + Utils.getStringFromThrowable(t), "fatal");
			throw new LYException("AsyncSession is stopped", t);
		} finally {
			Utils.tryClose(this);
		}
	}

	@Override
	public Socket getClient(InetAddr clientAddr) {
		return addr2client.get(clientAddr).socket();
	}

	@Override
	public Pair<byte[], Integer> receive(Socket socket) {
		if (isClosed())
			throw new LYException("Connection closed");
		if (socket == null)
			throw new NullPointerException("Parameter socket is null");
		byte[] buffer = new byte[maxBufferSize];
		int bufferLen = 0;
		int ret = 0;
		niobuf.clear();
		while (true) {
			try {
				ret = addr2client.get(Utils.getPeer(socket)).read(niobuf);
				if (ret <= 0) {
					if (ret == 0) {
						// move niobuf to buffer
						Utils.bytecat(buffer, bufferLen, niobuf.array(), 0, niobuf.position());
						bufferLen += niobuf.position();
						break;
					} else if (ret == -1)
						return null;
					else
						throw new LYException("IMPOSSIBLE? Socket returns:" + ret);
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
	
	public void send(Socket client, Confirm request) {
		send(client, protocol.encode(request));
	}

	public void send(Socket client, byte[] request) {
		if (isClosed())
			throw new LYException("Session closed");
		try {
			SocketChannel socketChannel = addr2client.get(Utils.getPeer(client));
			if (socketChannel != null)
				flushChannel(socketChannel, ByteBuffer.wrap(request), CoreDef.DEFAULT_SOCKET_WRITE_TTIMEOUT);
			else
				throw new LYException("No match client");
		} catch (Exception e) {
			throw new LYException("Send failed", e);
		}
	}

	private void flushChannel(SocketChannel socketChannel, ByteBuffer bb, long writeTimeout) throws Exception {
		SelectionKey key = null;
		Selector writeSelector = null;
		int torelent = 0;
		try {
			while (bb.hasRemaining()) {
				int len = socketChannel.write(bb);
				torelent++;
				if (len > 0)
					torelent = 0;
				else {
					if (writeSelector == null) {
						writeSelector = selectorPool.accessOne().getObject();
						if (writeSelector == null) {
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
	}

	@Override
	public void initialize() {
		if (isServer()) {
			SelectorCreator creator = new SelectorCreator();
			selectorPool = new AutoGeneratePool<ObjectContainer<Selector>>(creator, null,
					CoreDef.DEFAULT_CONTAINER_TIMEOUT, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
			begin("Async Session");
		}
		transfer.initialize();
	}
	
	@Override
	public void close() {
		try {
			if (isClosed()) return;
			for (InetAddr addr : addr2client.keySet()) {
				try {
					SocketChannel socketChannel = addr2client.get(addr);
					socketChannel.close();
				} catch (Exception e) {
					log.debug("Close failed, maybe client already lost connection" + Utils.getStringFromException(e));
				}
			}
			TimeoutController.removeFromWatch(this);
			addr2client.clear();
			Utils.tryClose(transfer, selector);
			transfer = null;
			selector = null;
			if (thread != null)
				callStop();
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return selector == null || !selector.isOpen();
	}

	// Recyclable
	@Override
	public boolean isRecyclable() {
		return !addr2client.isEmpty();
	}

	@Override
	public void recycle() {
		synchronized (lock) {
			Iterator<Entry<InetAddr, Long>> it = lastActivityMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<InetAddr, Long> entry = it.next();
				InetAddr addr = entry.getKey();
				Long last = entry.getValue();
				if (System.currentTimeMillis() - last > timeout) {
					SocketChannel tmp = addr2client.remove(addr);
					Utils.tryClose(tmp);
					it.remove();
				}
			}
		}
	}

	public long getSoTimeout() {
		return timeout;
	}

	public void setSoTimeout(long timeout) {
		this.timeout = timeout;
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
