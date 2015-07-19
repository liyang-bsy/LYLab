package net.vicp.lylab.utils.internet.async;

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
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.DoResponse;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.pool.RecyclePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.internet.BaseSocket;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.InfoSocket;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class AsyncSocket extends BaseSocket implements KeepAlive, LifeCycle, DoResponse {
	private static final long serialVersionUID = 883892527805494627L;

	// Raw data source
	protected Selector selector = null;
	protected SelectionKey selectionKey = null;
	protected SocketChannel socketChannel = null;
	protected Aop aop = null;
	// Token mapping
	protected Map<String, SocketChannel> ipMap = new ConcurrentHashMap<String, SocketChannel>();
	protected RecyclePool<Selector> selectorPool;

	// Long socket keep alive
	protected Map<String, Long> lastActivityMap = new ConcurrentHashMap<String, Long>();
	protected HeartBeat heartBeat;
	protected long interval = CoreDef.DEFAULT_SOCKET_TTIMEOUT/4;

	// Buffer
	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	
	/**
	 * Server mode
	 * @param port
	 */
	public AsyncSocket(int port, Aop aop, HeartBeat heartBeat) {
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.aop = aop;
			this.heartBeat = heartBeat;
			setIsServer(true);
		} catch (Exception e) {
			throw new LYException("Establish server failed", e);
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
			socketChannel = (SocketChannel) selectionKey.channel();
			try {
//				if (receive(socketChannel) == null) {
//					socketChannel.close();
//					return;
//				}
				doResponse(null);//buffer.array());
				
			} catch (Throwable t) {
				if(socketChannel != null)
				{
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
		
	}
	
	@Override
	public void exec() {
		try {
			while (true) {
				// Will be block here
				selector.select();
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
	public byte[] response(byte[] request, int offset) {
		if(aop == null)
			return null;
		Message requestMsg = null;
		Message responseMsg = null;
		try {
			Object obj = protocol.decode(request, offset);
			if(obj instanceof HeartBeat)
				return protocol.encode(heartBeat);
			requestMsg = (Message) obj;
		} catch (Exception e) {
			log.debug(Utils.getStringFromException(e));
		}
		if(requestMsg == null) {
			responseMsg = new Message();
			responseMsg.setCode(0x00001);
			responseMsg.setMessage("Message not found");
		}
		else
			responseMsg = aop.doAction(new InfoSocket(socketChannel .socket()), requestMsg);
		byte[] response = null;
		if(protocol != null) {
			response = protocol.encode(responseMsg);
			send(socketChannel, ByteBuffer.wrap(response));
		}
		return response;
	}
	
	public byte[] doResponse(byte[] request) {
		// 集成事件驱动
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		byte[] response = response(request, 0);
		if(afterTransmission != null)
			afterTransmission.callback(response);
		return response;
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
			if (socketChannel != null && flushChannel(socketChannel, request, CoreDef.DEFAULT_READ_TTIMEOUT)>0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}
	
	public ByteBuffer receive(SocketChannel socketChannel) {
		if(isClosed()) throw new LYException("Connection closed");
		if (socketChannel != null) {
			int bufferLen = 0;
			buffer.clear();
			int getLen = 0;
			while (true) {
				getLen = 0;
				try {
					if(bufferLen == buffer.array().length) {
						byte[] newBytes = Arrays.copyOf(buffer.array(), buffer.array().length*10);
						buffer = ByteBuffer.wrap(newBytes);
					}
					getLen = socketChannel.read(buffer);
					if (getLen == -1) return null;
				} catch (Exception e) {
					throw new LYException(e);
				}
				// Create a raw protocol after first receiving
				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
					protocol = ProtocolUtils.pairWithProtocol(buffer.array());
				bufferLen += getLen;
				
				if (protocol.validate(buffer.array(), bufferLen) > 0)
					break;
			}
		}
		return buffer;
	}

	@Override
	public void initialize() {
		if(config == null)
		{
			selectorPool = new SelectorPool(CoreDef.DEFAULT_CONTAINER_TIMEOUT,CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
		}
		else {
			selectorPool = new RecyclePool<Selector>(config.getInteger("maxSelectorPool"));
			for (int i = 0; i < config.getInteger("maxSelectorPool"); i++) {
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
	public void obtainConfig(Config config) {
		this.config = config;
	}
	
	@Override
	public void close() {
		try {
			if (isClosed()) return;
			if (thread != null)
				thread.interrupt();
			if (selector != null) {
				selector.close();
				selector = null;
			}
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
