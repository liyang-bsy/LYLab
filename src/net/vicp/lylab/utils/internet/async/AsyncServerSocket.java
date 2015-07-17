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
import java.util.Arrays;

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
public class AsyncServerSocket extends Task implements Recyclable, DoResponse {
	private static final long serialVersionUID = 883892527805494627L;
	
	// Raw data source
	protected Selector selector = null;
	protected ServerSocketChannel serverSocketChannel = null;
	protected ServerSocket serverSocket = null;
	
	protected Socket socket = null;

	// some thing about this socket
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected int port;

	// Buffer
	private ByteBuffer buffer = ByteBuffer.allocate(CoreDef.SOCKET_MAX_BUFFER);
	private int bufferLen = 0;
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
	public AsyncServerSocket(int port) {
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
			byte[] bytes = receive();
			if(bytes == null)
				return;
			send(doResponse(bytes));
			close();
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
	public byte[] response(byte[] request) {
		// do something
		return null;
	}
	
	public byte[] doResponse(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		if(!isServer()) throw new LYException("Do response is forbidden to a client socket");
		byte[] ret = response(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
	}
	
	public boolean send(ByteBuffer msg) throws Exception {
		if(isClosed()) return false;
		serverSocket.write(msg);
		serverSocket.flush();
		return true;
	}
	
	public byte[] receive() throws Exception {
		if(isClosed()) throw new LYException("Connection closed");
		if (in != null) {
			bufferLen = 0;
			Arrays.fill(buffer, (byte) 0);
			int getLen = 0;
			while (true) {
				getLen = 0;
				try {
					if(bufferLen == buffer.length)
						buffer = Arrays.copyOf(buffer, buffer.length*10);
					getLen = in.read(buffer, bufferLen, buffer.length - bufferLen);
					if (getLen == -1) return null;
				} catch (Exception e) {
					throw new LYException(e);
				}
				if (getLen == 0)
					throw new LYException("Impossible");
				// Create a raw protocol after first receiving
				if(bufferLen == 0 && (protocol == null || ProtocolUtils.isMultiProtocol()))
					protocol = ProtocolUtils.pairWithProtocol(buffer);
				bufferLen += getLen;
				if (protocol.validate(buffer, bufferLen))
					break;
			}
		}
		return buffer;
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
			in = null;
			out = null;
		}
		if(afterClose != null)
			afterClose.callback();
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed();
	}

	@Override
	public boolean isRecyclable() {
		return (socketRetry.get() < socketMaxRetry) && !isServer() && isClosed();
	}

	@Override
	public void recycle() {
		if (socketRetry.incrementAndGet() > socketMaxRetry)
			throw new LYException("Socket recycled for too many times");
		recycle(host, port);
	}

	protected void recycle(String host, int port) {
		if(isServer()) return;
		try {
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Recycle connection failed");
		}
	}

	// getters & setters below

}
