package net.vicp.lylab.utils.internet.async;

import java.io.EOFException;
import java.io.IOException;
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
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.DoResponse;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.pool.RecyclePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.config.Config;
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
public class AsyncSocket extends BaseSocket implements LifeCycle, InitializeConfig, DoResponse {
	private static final long serialVersionUID = 883892527805494627L;

	// Raw data source
	protected Selector selector = null;
	protected Selector writeSelector = null;
	protected AtomicBoolean closed = new AtomicBoolean(false);
	protected Aop aop = null;
	// Token mapping
	protected Map<String, SocketChannel> ipMap = new ConcurrentHashMap<String, SocketChannel>();
	protected RecyclePool<Selector> selectorPool;

	// Buffer
	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	
	/**
	 * Server mode
	 * @param port
	 */
	public AsyncSocket(int port, Aop aop) {
		try {
			selector = Selector.open();
			writeSelector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			selectorPool = new SelectorPool(CoreDef.DEFAULT_CONTAINER_TIMEOUT,CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
			this.aop = aop;
			setIsServer(true);
		} catch (Exception e) {
			throw new LYException("Establish server failed", e);
		}
	}

	public void selectionKeyHandler(SelectionKey selectionKey)
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
			try {
				SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
				if (receive(socketChannel) == null) {
					socketChannel.close();
					return;
				}
				byte[] response = doResponse(buffer.array());
				socketChannel.write(ByteBuffer.wrap(response));
			} catch (Exception e) {
				throw new LYException("Close failed", e);
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
					SelectionKey selectionKey = iterator.next();
					iterator.remove();
					selectionKeyHandler(selectionKey);
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
	public byte[] response(byte[] request) {
		if(aop == null) return request;
		byte[] response = aop.enterAction(protocol, this, request);
		return response;
	}
	
	public byte[] doResponse(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		byte[] ret = response(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
	}

	public void pushAll(byte[] data) {
		for(String ip:ipMap.keySet())
			push(ip, data);
	}

	public void push(String ip, byte[] data) {
		SocketChannel socketChannel = ipMap.get(ip);
		ByteBuffer msg = ByteBuffer.wrap(data);
		send(socketChannel, msg);
	}

	public int flushChannel(SocketChannel socketChannel, ByteBuffer bb,
			long writeTimeout) throws IOException {
		SelectionKey key = null;
		Selector writeSelector = selectorPool.accessOne();
		int attempts = 0;
		int bytesProduced = 0;
		try {
			while (bb.hasRemaining()) {
				int len = socketChannel.write(bb);
				attempts++;
				if (len < 0) {
					throw new EOFException();
				}
				bytesProduced += len;
				if (len == 0) {
					key = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
					if (writeSelector.select(writeTimeout) == 0) {
						if (attempts > 2) {
							socketChannel.close();
							throw new IOException("Client disconnected");
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
	
	public boolean send(SocketChannel socketChannel, ByteBuffer msg) {
		try {
			if (isClosed())
				return false;
			if (socketChannel != null) {
				flushChannel(socketChannel, msg, CoreDef.DEFAULT_READ_TTIMEOUT);
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
	public void start() {
		selectorPool = new RecyclePool<Selector>(16);
		for (int i = 0; i < config.getInteger("maxSize"); i++) {
			try {
				selectorPool.add(Selector.open());
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
		this.begin("AsyncServer");
	}

	@Override
	public void obtainConfig(Config config) {
		this.config = config;
	}
	
	@Override
	public void close() {
		try {
			if(closed.getAndSet(true)) return;
			if (thread != null)
				thread.interrupt();
			if (selector != null) {
				selector.close();
				selector = null;
			}
			if (writeSelector != null) {
				writeSelector.close();
				writeSelector = null;
			}
			if(afterClose != null)
				afterClose.callback();
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return closed.get();
	}

	// getters & setters below

}
