package net.vicp.lylab.utils.internet.async_test;

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
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.ObjectContainer;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.core.pool.RecyclePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.creator.SelectorCreator;
import net.vicp.lylab.utils.internet.BaseSocket;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

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
public class AsyncSocket extends BaseSocket implements LifeCycle, Transmission {
	private static final long serialVersionUID = -3262692917974231303L;
	
	// Raw data source
	protected Selector selector = null;
	protected SelectionKey selectionKey = null;
	// Not null unless isServer() == false
	protected SocketChannel socketChannel = null;
	
	// TODO will be useful on push data to client
	// Clients		ip			Socket
	protected Map<String, SocketChannel> ip2client = new HashMap<>();
	protected RecyclePool<ObjectContainer<Selector>> selectorPool;
	protected Transfer transfer;
	// = new Transfer(aopLogic);

	// Long socket keep alive
	protected Map<String, Long> lastActivityMap = new ConcurrentHashMap<String, Long>();
	protected HeartBeat heartBeat;
	protected long interval = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT/10;

	// Buffer
	private ByteBuffer niobuf = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	private byte[] buffer = new byte[CoreDef.SOCKET_MAX_BUFFER];
	private int maxBufferSize = CoreDef.SOCKET_MAX_BUFFER;
	
	/**
	 * <b>[Server mode]</b><br>
	 * Async is only useful on Long Socket
	 * 
	 * @param port
	 * @param heartBeat
	 */
	public AsyncSocket(int port, HeartBeat heartBeat) {
		super.setLonewolf(true);
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
				ip2client.put(socket.getInetAddress().getHostAddress(), socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				throw new LYException("Close failed", e);
			}
		} else if (selectionKey.isReadable()) {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			try {
//				receiveBasedAopDrive(socketChannel);
				receive(socketChannel);
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

	@Override
	public byte[] response(Socket client, byte[] request, int offset) {
		return getAopLogic().doAction(client, request, offset);
	}
	
	private byte[] doResponse(Socket client, byte[] request, int offset) {
//		if(beforeTransmission != null)
//			beforeTransmission.callback(request);
//		byte[] response = 
		response(client, request, 0);
//		if(afterTransmission != null)
//			afterTransmission.callback();//response);
		return null;//response;
	}

	/**
	 * @param request
	 * @return
	 */
	@Override
	public byte[] request(byte[] request) {
		return null;
	}

	// Reply will be found async
	public void doRequest(byte[] request) {
		request(request);
	}
	
	private byte[] bytecat(byte[] pre, int preOffset, int preCopyLenth, byte[] suf, int sufOffset, int sufCopyLenth) {
		byte[] out = new byte[preCopyLenth - preOffset - sufOffset + sufCopyLenth];
		int i;
		for (i = preOffset; i < preCopyLenth; i++)
			out[i - preOffset] = pre[i];
		for (int j = sufOffset; j < sufCopyLenth; j++)
			out[i - preOffset - sufOffset + j] = suf[j];
		return out;
	}
	
	private void bytecat(byte[] dst, int dstOffset, byte[] src, int srcOffset) {
		for (int i = 0; i < src.length - srcOffset; i++)
			dst[dstOffset + i] = src[srcOffset + i];
	}

	public byte[] receive(SocketChannel socketChannel) {
		if (isClosed())
			throw new LYException("Connection closed");
		if (socketChannel != null) {
			int bufferLen = 0;
			Arrays.fill(buffer, (byte) 0);
			niobuf.clear();
			int getLen = 0;
			while (true) {
				getLen = 0;
				try {
					if (bufferLen == buffer.length) {
						maxBufferSize *= 10;
						buffer = Arrays.copyOf(buffer, buffer.length * 10);
					}
					getLen = socketChannel.read(niobuf);
					niobuf.get(buffer, bufferLen, buffer.length);
					if(niobuf.remaining() == 0) {
						byte[] newBytes = new byte[niobuf.capacity() * 10];
						// transfer bytes from this buffer into the given destination array
						niobuf.get(newBytes, 0, niobuf.capacity());
						// extend ByteBuffer
						niobuf = ByteBuffer.wrap(newBytes);
					}
					if (getLen == 0)
						continue;
					if (getLen == -1)
						return null;
				} catch (Exception e) {
					throw new LYException(e);
				}
				// Create a raw protocol after first receiving
				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
					protocol = ProtocolUtils.pairWithProtocol(buffer);
				bufferLen += getLen;
				if (protocol.validate(buffer, bufferLen) > 0)
					break;
			}
		}
		return buffer;
	}

//	private void receiveBasedAopDrive(SocketChannel socketChannel) throws Exception {
//		if (isClosed())
//			throw new LYException("Connection closed");
//		byte[] readTail = new byte[CoreDef.SOCKET_MAX_BUFFER];
//		int readTailLen = 0;
//		if (socketChannel != null) {
//			int bufferLen = 0;
//			int getLen = 0;
//			int torelent = 0;
//			while (true) {
//				buffer.clear();
//				getLen = socketChannel.read(buffer);
//				if (getLen == 0) {
//					if (torelent > 3) {
//						throw new LYException("Lost connection to client");
//					}
//					torelent++;
//					continue;
//				}
//				if (getLen == -1)
//				{
////					if(protocol!=null)
////						send(socketChannel, ByteBuffer.wrap(protocol.encode(new SimpleHeartBeat())));
//					break;
//				}
//				getLen += readTailLen;
//
//				if(buffer.remaining() == 0) {
//
//					byte[] newBytes = new byte[buffer.capacity() * 10];
//					// transfer bytes from this buffer into the given destination array
//					buffer.get(newBytes, 0, buffer.capacity());
////					byte[] newBytes = Arrays.copyOf(buffer.array(), buffer.array().length*10);
//					buffer = ByteBuffer.wrap(newBytes);
//					readTail = Arrays.copyOf(readTail, readTail.length*10);
//				}
//				
//				byte[] request = bytecat(readTail, 0, readTailLen, buffer.array(),
//						0, buffer.array().length);
//				readTailLen = 0;
//
//				// Create a raw protocol after first receiving
//				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
//					protocol = ProtocolUtils.pairWithProtocol(buffer.array());
//				
//				int offset = 0, next = 0;
//				while ((next = protocol.validate(request, offset, getLen)) != 0) {
//					doResponse(socketChannel.socket(), request, offset);
////					send(socketChannel, ByteBuffer.wrap(protocol.encode(new SimpleConfirm(0))));
//					offset = next;
//				}
//				if (offset == getLen)
//					break;
//				// Reserve tail
//				readTailLen = getLen - offset;
//				for (int i = offset; i < getLen; i++) {
//					readTail[i - offset] = request[i];
//				}
//				bufferLen += offset;
//			}
//		}
//	}
	
//	public void requestAll(byte[] data) {
//		for(String ip:ipMap.keySet())
//			request(ip, data);
//	}

//	public byte[] request(String ip, byte[] data) {
//		SocketChannel socketChannel = ipMap.get(ip);
//		ByteBuffer msg = ByteBuffer.wrap(data);
//		send(socketChannel, msg);
//		return null;
//	}
	
	protected boolean send(SocketChannel socketChannel, byte[] request) {
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
		// T-O-D-O
//		if(!CoreDef.config.containsKey("AsyncSocket")) {
			SelectorCreator creator = new SelectorCreator();
			selectorPool = new AutoGeneratePool<ObjectContainer<Selector>>(creator, null, CoreDef.DEFAULT_CONTAINER_TIMEOUT, CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
//		}
//		else {
//			selectorPool = new RecyclePool<ObjectContainer<Selector>>(CoreDef.config.getConfig("AsyncSocket").getInteger("maxSelectorPool"));
//			for (int i = 0; i < CoreDef.config.getConfig("AsyncSocket").getInteger("maxSelectorPool"); i++) {
//				try {
//					selectorPool.add(ObjectContainer.fromObject(Selector.open()));
//				} catch (Exception e) {
//					log.error(Utils.getStringFromException(e));
//				}
//			}
//		}
		begin("AsyncServer");
	}
	
	@Override
	public void close() {
		try {
			if (isClosed()) return;
			for (String ip : ip2client.keySet()) {
				try {
					SocketChannel socketChannel = ip2client.get(ip);
					socketChannel.socket().close();
				} catch (Exception e) {
					log.debug("Close failed, maybe client already lost connection" + Utils.getStringFromException(e));
				}
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
