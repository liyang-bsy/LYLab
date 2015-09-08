package net.vicp.lylab.utils.internet;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.RecyclePool;
import net.vicp.lylab.server.transport_give_up.Transport;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

/**
 * A async socket can be used for communicating with paired client.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.21
 * @version 0.0.5
 */
public class AsyncSocket extends BaseSocket implements KeepAlive, LifeCycle, Transmission {
	private static final long serialVersionUID = -3262692917974231303L;
	
	// Raw data source
	protected Selector selector = null;
	protected SelectionKey selectionKey = null;
	// Not null unless isServer() == false
	protected SocketChannel socketChannel = null;
	
	// IP mapping
	protected Map<String, SocketChannel> ipMap = new ConcurrentHashMap<String, SocketChannel>();
	protected RecyclePool<Selector> selectorPool;
	protected Transport transport;

	// Long socket keep alive
	protected Map<String, Long> lastActivityMap = new ConcurrentHashMap<String, Long>();
	protected HeartBeat heartBeat;
	protected long interval = CoreDef.DEFAULT_SOCKET_TTIMEOUT/4;

	// Buffer
	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	private byte[] readTail = new byte[CoreDef.SOCKET_MAX_BUFFER];
	private int readTailLen = 0;

	/**
	 * Server mode
	 * @param port
	 */
	public AsyncSocket(int port, HeartBeat heartBeat) {
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.heartBeat = heartBeat;
			setIsServer(true);
		} catch (Exception e) {
			throw new LYException("Establish server failed", e);
		}
	}
	
	/**
	 * Client mode
	 * @param port
	 */
	public AsyncSocket(String host, int port, HeartBeat heartBeat) {
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
			this.heartBeat = heartBeat;
			setIsServer(false);
		} catch (Exception e) {
			throw new LYException("Connect to server failed", e);
		}
	}

	public void selectionKeyHandler()
	{
		if (selectionKey.isAcceptable()) {
			try {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
				SocketChannel socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				Socket socket = socketChannel.socket();
				ipMap.put(socket.getInetAddress().getHostAddress(), socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				throw new LYException("Close failed", e);
			}
		} else if (selectionKey.isReadable()) {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			try {
				receiveBasedAopDrive(socketChannel);
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
			while (selector.select() > 0) {
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
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
//		byte[] response = 
		response(client, request, 0);
		if(afterTransmission != null)
			afterTransmission.callback();//response);
		return null;//response;
	}

	/**
	 * do 
	 * @param request
	 * @return
	 */
	@Override
	public byte[] request(byte[] request) {
		send(socketChannel, ByteBuffer.wrap(request));
		return null;
	}

	// Reply will be found async
	public void doRequest(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		request(request);
		if(afterTransmission != null)
			afterTransmission.callback();
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

//	public ByteBuffer receive(SocketChannel socketChannel) {
//		if(isClosed()) throw new LYException("Connection closed");
//		if (socketChannel != null) {
//			int bufferLen = 0;
//			buffer.clear();
//			int getLen = 0;
//			while (true) {
//				getLen = 0;
//				try {
//					if(bufferLen == buffer.array().length) {
//						byte[] newBytes = Arrays.copyOf(buffer.array(), buffer.array().length*10);
//						buffer = ByteBuffer.wrap(newBytes);
//					}
//					getLen = socketChannel.read(buffer);
//					if (getLen == -1) return null;
//				} catch (Exception e) {
//					throw new LYException(e);
//				}
//				// Create a raw protocol after first receiving
//				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
//					protocol = ProtocolUtils.pairWithProtocol(buffer.array());
//				bufferLen += getLen;
//				
//				if (protocol.validate(buffer.array(), bufferLen) > 0)
//					break;
//			}
//		}
//		return buffer;
//	}
	
	private void receiveBasedAopDrive(SocketChannel socketChannel) throws Exception {
		if (isClosed())
			throw new LYException("Connection closed");
		if (socketChannel != null) {
			int bufferLen = 0;
			int getLen = 0;
			int attempts = 0;
			while (true) {
				buffer.clear();
				getLen = socketChannel.read(buffer);
				if (getLen == 0) {
					if (attempts > 3) {
						try {
							socketChannel.close();
						} catch (Exception e) {
							throw new LYException("Lost connection to client, and close socket channel failed", e);
						}
						throw new LYException("Lost connection to client");
					}
					attempts++;
					continue;
				}
				if (getLen == -1)
				{
					if(protocol!=null)
						send(socketChannel, ByteBuffer.wrap(protocol.encode(new SimpleHeartBeat())));
					break;
				}
				getLen += readTailLen;

				if(bufferLen == buffer.array().length) {
					byte[] newBytes = Arrays.copyOf(buffer.array(), buffer.array().length*10);
					buffer = ByteBuffer.wrap(newBytes);
					readTail = Arrays.copyOf(readTail, readTail.length*10);
				}
				
				byte[] request = bytecat(readTail, 0, readTailLen, buffer.array(),
						0, buffer.array().length);
				readTailLen = 0;

				// Create a raw protocol after first receiving
				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
					protocol = ProtocolUtils.pairWithProtocol(buffer.array());
				
				int offset = 0, next = 0;
				while ((next = protocol.validate(request, offset, getLen)) != 0) {
					doResponse(socketChannel.socket(), request, offset);
//					send(socketChannel, ByteBuffer.wrap(protocol.encode(new SimpleConfirm(0))));
					offset = next;
				}
				if (offset == getLen)
					break;
				// Reserve tail
				readTailLen = getLen - offset;
				for (int i = offset; i < getLen; i++) {
					readTail[i - offset] = request[i];
				}
				bufferLen += offset;
			}
		}
	}
	
	public void requestAll(byte[] data) {
		for(String ip:ipMap.keySet())
			request(ip, data);
	}

	public byte[] request(String ip, byte[] data) {
		SocketChannel socketChannel = ipMap.get(ip);
		ByteBuffer msg = ByteBuffer.wrap(data);
		send(socketChannel, msg);
		return null;
	}

	private int flushChannel(SocketChannel socketChannel, ByteBuffer bb,
			long writeTimeout) throws Exception {
		SelectionKey key = null;
		Selector writeSelector = null;
		int attempts = 0;
		int bytesProduced = 0;
		try {
			while (bb.hasRemaining()) {
				int len = socketChannel.write(bb);
				attempts++;
				bytesProduced += len;
				if (len == 0) {
	                if (writeSelector == null){
						writeSelector = selectorPool.accessOne();
	                    if (writeSelector == null){
	                        // Continue using the main one
	                        continue;
	                    }
	                }
					key = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
					if (writeSelector.select(writeTimeout) == 0) {
						if (attempts > 2) {
							try {
								socketChannel.close();
							} catch (Exception e) {
								throw new LYException("Lost connection to client, and close socket channel failed", e);
							}
							throw new LYException("Lost connection to client");
						}
					} else {
						attempts--;
					}
				} else {
					attempts = 0;
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
				selectorPool.recycle(writeSelector);
			}
		}
		return bytesProduced;
	}
	
	protected boolean send(SocketChannel socketChannel, ByteBuffer request) {
		try {
			if (isClosed())
				return false;
			if (socketChannel != null && flushChannel(socketChannel, request, CoreDef.DEFAULT_READ_TTIMEOUT)>0)
				return true;
			return false;
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	@Override
	public void initialize() {
		// TODO
		if(!CoreDef.config.containsKey("AsyncSocket")) {
			selectorPool = new SelectorPool(CoreDef.DEFAULT_CONTAINER_TIMEOUT,CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
		}
		else {
			selectorPool = new RecyclePool<Selector>(CoreDef.config.getConfig("AsyncSocket").getInteger("maxSelectorPool"));
			for (int i = 0; i < CoreDef.config.getConfig("AsyncSocket").getInteger("maxSelectorPool"); i++) {
				try {
					selectorPool.add(Selector.open());
				} catch (Exception e) {
					log.error(Utils.getStringFromException(e));
				}
			}
		}
		begin("AsyncServer");
	}
	
	@Override
	public void close() {
		try {
			if (isClosed()) return;
			for (String ip : ipMap.keySet()) {
				try {
					SocketChannel socketChannel = ipMap.get(ip);
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
			if (afterClose != null)
				afterClose.callback();
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return !selector.isOpen();
	}

	// Keep alive
	@Override
	public void setInterval(long interval) {
		this.interval = interval;
	}

	@Override
	public boolean isDying() {
		long earliest = Long.MAX_VALUE;
		for (String key : lastActivityMap.keySet()) {
			long tmp = lastActivityMap.get(key);
			if (tmp < earliest)
				earliest = tmp;
		}
		if(System.currentTimeMillis() - earliest > interval)
			return true;
		return false;
	}

	@Override
	public boolean keepAlive() {
		if(!isDying()) return true;
		try {
			if(protocol == null)
				return true;
			List<String> keepAliveList = new ArrayList<String>();
			for (String key : lastActivityMap.keySet()) {
				long lastActivity = lastActivityMap.get(key);
				if (System.currentTimeMillis() - lastActivity > interval) {
					keepAliveList.add(key);
				}
			}
			for(String ip:keepAliveList)
				try {
					byte[] bytes = request(ip, protocol.encode(heartBeat));
					if (bytes != null) {
						Object obj = protocol.decode(bytes);
						if (obj instanceof HeartBeat)
							return true;
						else
							log.error("Send heartbeat failed\n" + obj.toString());
					}
				} catch (Exception e) {
					SocketChannel socketChannel = ipMap.get(ip);
					try {
						socketChannel.close();
					} catch (Exception ex) {
						log.error(Utils.getStringFromException(ex));
					}
				}
			return true;
		} catch (Exception e) {
			log.error("This socket may be dead" + Utils.getStringFromException(e));
		}
		return false;
	}

	@Override
	public boolean isAlive() {
		if (isClosed())
			return false;
		if(!keepAlive()) return false;
		return true;
	}

}
