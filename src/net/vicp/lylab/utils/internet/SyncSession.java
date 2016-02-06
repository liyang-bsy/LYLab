package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.dispatch.DispatchExecutor;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncSession extends AbstractSession implements KeepAlive {
	private static final long serialVersionUID = -6688729709782669561L;

	// Raw data source
	protected Socket socket;
	protected InputStream in;
	protected OutputStream out;

//	protected Transfer transfer;

	// Buffer
	protected byte[] buffer = new byte[CoreDef.SOCKET_MAX_BUFFER];

	// Long socket needs keep alive
	protected long lastActivity = 0L;
	protected long interval = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT / 10;

	public SyncSession(ServerSocket serverSocket, Protocol protocol,
			Dispatcher<? super Confirm, ? super Confirm> dispatcher) {
		this(serverSocket, protocol, dispatcher, null);
	}

	public SyncSession(String host, Integer port, Protocol protocol) {
		this(host, port, protocol, null);
	}
	
	/**
	 * Server mode
	 * 
	 * @param serverSocket
	 * @param protocol
	 * @param dispatcher
	 * @param heartBeat
	 */
	public SyncSession(ServerSocket serverSocket, Protocol protocol, Dispatcher<? super Confirm, ? super Confirm> dispatcher,
			HeartBeat heartBeat) {
		super(protocol, dispatcher, heartBeat);
		if (serverSocket == null)
			throw new LYException("Parameter serverSocket is null");
		if (dispatcher == null)
			throw new LYException("Parameter dispatcher is null");
		try {
			this.socket = serverSocket.accept();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("ServerSocket accept from client socket failed", e);
		}
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Can not open input/output stream from client socket", e);
		}
		setServer(true);
	}
	
	/**
	 * Client mode
	 * @param host
	 * @param port
	 */
	public SyncSession(String host, Integer port, Protocol protocol, HeartBeat heartBeat) {
		super(protocol, null, heartBeat);
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), CoreDef.DEFAULT_SOCKET_CONNECT_TTIMEOUT);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
		setServer(false);
	}

	public InetAddr getPeer() {
		if(isServer())
			// client ip + local port(a random port assigned by operation system)
			return InetAddr.fromInetAddr(socket.getInetAddress().getHostAddress(), socket.getPort());
		else
			// server ip + server port
			return InetAddr.fromInetAddr(socket.getInetAddress().getHostAddress(), socket.getPort());
	}

	@Override
	public Socket getClient(InetAddr clientAddr) {
		if(getPeer().equals(clientAddr))
			return socket;
		throw new LYException("No match client");
	}

	@Override
	public void exec() {
		if (!isServer())
			throw new LYException("Method forbidden");
		try {
			do {
				Pair<byte[], Integer> bytesContainer = receive(socket);
				if (bytesContainer == null)
					return;
//				transfer.putRequest(socket, buffer, bytesContainer.getRight());
//				DispatchExecutor<? super Confirm, ? super Confirm> de = new DispatchExecutor<>(socket, bytesContainer.getLeft(), this, dispatcher,
//						protocol);
				byte[] bytes = DispatchExecutor.doResponse(socket, bytesContainer.getLeft(), this, dispatcher, protocol);
				if (bytes == null)
					throw new LYException("Server attempt respond null to client");
				send(bytes);
			} while (heartBeat != null);
		} catch (Throwable t) {
			throw new LYException("Connect break", t);
		} finally {
			try {
				close();
			} catch (Exception e) {
				log.info(Utils.getStringFromException(e));
			}
		}
	}

	@Override
	public void initialize() {
		// do nothing
	}

	public void send(byte[] request) {
		send(socket, request);
	}
	
	@Override
	public void send(Socket client, byte[] request) {
		send(client, request, 0, request.length);
	}
	
	public void send(Socket client, byte[] request, int offset, int length) {
		if (client == null)
			throw new NullPointerException("Parameter client is null");
		if (request == null)
			throw new NullPointerException("Parameter request is null");
		if(isClosed()) 
			throw new LYException("Session closed");
		if(!client.equals(socket))
			throw new LYException("Session client not matched");
		try {
			out.write(request, offset, length);
			out.flush();
		} catch (Exception e) {
			throw new LYException("Send failed", e);
		}
	}

	public Pair<byte[], Integer> receive() {
		return receive(socket);
	}
	
	@Override
	public Pair<byte[], Integer> receive(Socket client) {
		if (isClosed())
			throw new LYException("Session closed");
		if (!client.equals(socket))
			throw new LYException("Session client not matched");
		int bufferLen = 0;
		if (in != null) {
			Arrays.fill(buffer, (byte) 0);
			int getLen = 0;
			while (true) {
				getLen = 0;
				try {
					if(bufferLen == buffer.length)
						buffer = Arrays.copyOf(buffer, buffer.length*CoreDef.SOCKET_MAX_BUFFER_EXTEND_RATE);
					getLen = in.read(buffer, bufferLen, buffer.length - bufferLen);
					bufferLen += getLen;
					if (getLen == -1) return null;
				} catch (Exception e) {
					throw new LYException(e);
				}
				if (getLen == 0)
					throw new LYException("Impossible");
				// validate if receive finished
				if (protocol.validate(buffer, bufferLen) > 0)
					break;
			}
		}
		return new Pair<>(buffer, bufferLen);
	}
	
	@Override
	public void close() {
		try {
			if (socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				if(!socket.isClosed())
					socket.close();
				socket = null;
				in = null;
				out = null;
			}
			if (thread != null)
				thread.interrupt();
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed() || in == null || out == null;
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
	
	@Override
	public void setInterval(long interval) {
		this.interval = interval;
	}

	@Override
	public boolean isOutdated() {
		synchronized (lock) {
			if (System.currentTimeMillis() - lastActivity > interval)
				return true;
			return false;
		}
	}

	@Override
	public boolean keepAlive() {
		synchronized (lock) {
			if (!isOutdated())
				return true;
			try {
				send(socket, protocol.encode(heartBeat));
				Pair<byte[], Integer> data = receive(socket);
				byte[] bytes = data.getLeft();
				if (bytes != null) {
					Object obj = protocol.decode(bytes);
					if (obj instanceof HeartBeat)
						return true;
					else
						log.error("Send heartbeat failed\n" + obj.toString());
				}
			} catch (Exception e) {
				log.error("This socket may have dead" + Utils.getStringFromException(e));
			}
			return false;
		}
	}

	@Override
	public boolean isAlive() {
		if (isClosed())
			return false;
		if (!keepAlive())
			return false;
		return true;
	}

}
