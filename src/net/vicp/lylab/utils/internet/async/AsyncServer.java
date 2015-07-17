package net.vicp.lylab.utils.internet.async;

import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Set;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.DoResponse;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicInteger;
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
public class AsyncServer extends Task implements AutoCloseable, DoResponse {
	private static final long serialVersionUID = 883892527805494627L;
	
	// Raw data source
	protected Selector selector = null;
	protected ServerSocketChannel serverSocketChannel = null;
	protected ServerSocket serverSocket = null;
	protected Socket socket = null;
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
	public AsyncServer(int port) {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			serverSocketChannel.register(selector, SelectionKey.OP_READ);
			serverSocketChannel.register(selector, SelectionKey.OP_WRITE);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}

	@Override
	public void exec() {
		try {
			while (true) {
				// Will be block here
				selector.select();
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectionKeys.iterator();
				SocketChannel socketChannel;
				
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					// 判断事件类型
					if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
						ServerSocketChannel nssc = (ServerSocketChannel) key.channel();
						socketChannel = nssc.accept();
						// 设为非阻塞
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ);
						iter.remove();
//						System.out.println("有新的链接" + socketChannel);
					} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
						socketChannel = (SocketChannel) key.channel();
						while (true) {
							int a = socketChannel.read(buffer);
							if (a == -1)
								break;
							if (a > 0) {
								Object o = p.decode(buffer);
								socketChannel.write(ByteBuffer.wrap(p.encode(o)));
								break;
							}
						}

						iter.remove();
					}
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
	
	public boolean send(SocketChannel socketChannel, byte[] msg) throws Exception {
		if (isClosed())
			return false;
		if (socketChannel != null) {
			socketChannel.write(ByteBuffer.wrap(msg));
			return true;
		}
		return false;
	}
	
	public byte[] receive(SocketChannel socketChannel) throws Exception {
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
		return buffer.array();
	}

	@Override
	public void close() throws Exception {
		if (thread != null)
			thread.interrupt();
		if (socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			if(!socket.isClosed())
				socket.close();
			socket = null;
		}
		if(afterClose != null)
			afterClose.callback();
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed();
	}

	// getters & setters below

}
