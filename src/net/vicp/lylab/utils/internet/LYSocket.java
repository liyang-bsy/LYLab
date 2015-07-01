package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.interfaces.callback.AfterEnd;
import net.vicp.lylab.core.interfaces.callback.BeforeStart;
import net.vicp.lylab.core.interfaces.recycle.Recyclable;
import net.vicp.lylab.utils.ByteUtils;
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
public class LYSocket extends Task implements Recyclable, AutoCloseable, Transmission {
	private static final long serialVersionUID = 883892527805494627L;
	
	protected Socket socket;
	
	protected BeforeStart beforeStart = null;
	protected AfterEnd afterEnd = null;
	
	protected InputStream in;
	protected OutputStream out;
	
	private boolean isServer;
	
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected String host;
	protected int port;
	
	public LYSocket(ServerSocket serverSocket) {
		if(serverSocket == null) throw new LYException("Parameter serverSocket is null");
		try {
			this.socket = serverSocket.accept();
			in = socket.getInputStream();
			out = socket.getOutputStream();
			isServer = true;
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public LYSocket(String host, Integer port) {
		this.host = host;
		this.port = port;
		isServer = false;
	}

	@Override
	public void exec() {
		try {
			if (isServer()) {
				byte[] bytes = receive();
				if(bytes == null) return;
				send(doResponse(bytes));
			} else {
				doRequest(null);
			}
		} catch (Exception e) {
			throw new LYException("Connect break", e);
		}
	}

	@Override
	public byte[] request(byte[] request) {
		// do something
		return null;
	}

	@Override
	public byte[] response(byte[] request) {
		// do something
		return null;
	}
	
	public byte[] doRequest(byte[] request) {
		if(isServer()) throw new LYException("Do request is forbidden to a server socket");
		return request(request);
	}

	public byte[] doResponse(byte[] request) {
		if(!isServer()) throw new LYException("Do response is forbidden to a client socket");
		return response(request);
	}
	
	public void connect()
	{
		if(isServer()) return;
		try {
			if(beforeStart != null)
				beforeStart.beforeStart();
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}
	
	public boolean send(byte[] msg) throws Exception {
		if(isClosed()) return false;
		out.write(msg);
		out.flush();
		return true;
	}

	public byte[] receive() throws Exception {
		if(isClosed()) throw new LYException("Connection closed");
		List<Byte> container = new ArrayList<Byte>();
		if (in != null) {
			byte[] rc = new byte[CoreDef.SOCKET_MAX_BUFFER];
			int totalRecv = 0, getLen = 0;
			while (true) {
				try {
					getLen = in.read(rc, 0, CoreDef.SOCKET_MAX_BUFFER);
				} catch (Exception e) {
					throw new LYException(e);
				}
				if (getLen == -1)
					return null;
				if (getLen == 0)
					throw new LYException("Impossible");
				totalRecv += getLen;
				container.addAll(ByteUtils.moveBytesToContainer(rc));
				int result = ProtocolUtils.validate(ByteUtils.copyBytesFromContainer(container),totalRecv);
				if (result == -1)
					throw new LYException("Bad data package");
				if (result == 0)
					break;
				if (result == 1)
					continue;
			}
		}
		return ByteUtils.copyBytesFromContainer(container);
	}

	@Override
	public void close() throws Exception {
		if (socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			if(!socket.isClosed())
				socket.close();
			socket = null;
			in = null;
			out = null;
		}
		if(afterEnd != null)
			afterEnd.afterEnd();
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed()
				|| in == null || out == null;
	}

	@Override
	public boolean isRecyclable() {
		return !isServer() && isClosed();
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
	
	protected void waitCycle()
	{
		synchronized (lock) {
			try {
				lock.wait(CoreDef.WAITING_LONG);
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			};
		}
	}
	
	protected void interrupt()
	{
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void setSoTimeout(int timeout)
	{
		try {
			socket.setSoTimeout(timeout);
		} catch (Exception e) {
            throw new LYException("Set timeout failed", e);
		}
	}
	
	public int getSoTimeout()
	{
		try {
			return socket.getSoTimeout();
		} catch (Exception e) {
			throw new LYException("Socket is closed");
		}
	}

	// getters & setters below
	public int getSocketMaxRetry() {
		return socketMaxRetry;
	}

	public void setSocketMaxRetry(int socketMaxRetry) {
		this.socketMaxRetry = socketMaxRetry;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isServer() {
		return isServer;
	}

	public int getSocketRetry() {
		return socketRetry.get();
	}

	public BeforeStart getBeforeStart() {
		return beforeStart;
	}

	public void setBeforeStart(BeforeStart beforeStart) {
		this.beforeStart = beforeStart;
	}

	public AfterEnd getAfterEnd() {
		return afterEnd;
	}

	public void setAfterEnd(AfterEnd afterEnd) {
		this.afterEnd = afterEnd;
	}

}
