package net.vicp.lylab.utils.internet.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.DoResponse;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.ConnectionPool;
import net.vicp.lylab.utils.internet.impl.Message;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class AsyncSocket extends Task implements AutoCloseable, DoResponse {
	private static final long serialVersionUID = 883892527805494627L;

	// Raw data source
	protected Selector selector = null;
	protected AtomicBoolean closed = new AtomicBoolean(false);
//	protected ServerSocketChannel serverSocketChannel = null;
//	protected ServerSocket serverSocket = null;
//	protected Socket socket = null;
//	protected ConnectionPool<SocketChannel> cp = new ConnectionPool<SocketChannel>(SocketChannel.class, host, port, protocol, heartBeat)
	protected Map<String, SocketChannel> map = new ConcurrentHashMap<String, SocketChannel>();

	// Some thing about this socket
	private boolean isServer;
	protected String host;
	protected int port;

	// Buffer
	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	protected Protocol protocol = null;

	// Callback below
	protected Callback beforeConnect = null;
	protected Callback afterClose = null;
	protected Callback beforeTransmission = null;
	protected Callback afterTransmission = null;
	
	/**
	 * Server mode
	 * @param port
	 */
	public AsyncSocket(int port) {
		try {
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			isServer = true;
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public void selectionKeyHandler(SelectionKey selectionKey) throws Exception
	{
		if (selectionKey.isAcceptable()) {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);
			socketChannel.socket();
			socketChannel.register(selector,SelectionKey.OP_READ);
			// 新增一个链接
		} else if (selectionKey.isReadable()) {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			if(receive(socketChannel)==null) {
				socketChannel.close();
				return;
			}
			System.out.println(socketChannel.getRemoteAddress() + " ：" + protocol.decode(buffer.array()));
			Object obj = protocol.decode(buffer.array());
			// 根据客户端的消息，查找到对应的输出
			socketChannel.write(ByteBuffer.wrap(protocol.encode(obj)));

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
		// do something
		return null;
	}
	
	public byte[] doResponse(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		byte[] ret = response(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
	}
	
	public boolean send(SocketChannel socketChannel, ByteBuffer msg) throws Exception {
		if (isClosed())
			return false;
		if (socketChannel != null) {
			socketChannel.write(msg);
			return true;
		}
		return false;
	}
	
	public ByteBuffer receive(SocketChannel socketChannel) throws Exception {
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
				if (protocol.validate(buffer.array(), bufferLen))
					break;
			}
		}
		return buffer;
	}

	@Override
	public void close() throws Exception {
		if(closed.getAndSet(true)) return;
		if (thread != null)
			thread.interrupt();
		if (selector != null) {
			selector.close();
			selector = null;
		}
		if(afterClose != null)
			afterClose.callback();
	}

	public boolean isClosed() {
		return closed.get();
	}

	// getters & setters below

}
