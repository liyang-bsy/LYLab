package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.KeepAlive;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transfer;
import net.vicp.lylab.core.model.HeartBeat;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.Utils;

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
	
	protected Transfer transfer;

	// Buffer
	protected byte[] buffer = new byte[CoreDef.SOCKET_MAX_BUFFER];

	// Long socket needs keep alive
	protected HeartBeat heartBeat;
	protected long lastActivity = 0L;
	protected long interval = CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT / 10;


	public SyncSession(ServerSocket serverSocket, HeartBeat heartBeat, Transfer transfer) {
		this(serverSocket, transfer);
		this.heartBeat = heartBeat;
	}

	public SyncSession(String host, Integer port, HeartBeat heartBeat) {
		this(host, port);
		this.heartBeat = heartBeat;
	}
	
	public SyncSession(ServerSocket serverSocket, Transfer transfer) {
		if (serverSocket == null)
			throw new LYException("Parameter serverSocket is null");
		if (transfer == null)
			throw new LYException("Parameter transfer is null");
		try {
			this.socket = serverSocket.accept();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_READ_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("ServerSocket accept from client socket failed", e);
		}
		this.transfer = transfer;
		transfer.setSession(this);
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
	public SyncSession(String host, Integer port) {
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

	protected InetSocketAddress getPeer() {
		// If not, I can't control current remote information
		return (InetSocketAddress) socket.getRemoteSocketAddress();
	}

	@Override
	public void exec() {
		throw new LYException("Method forbidden");
	}
	
	@Override
	public void initialize() {
		transfer.initialize();
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
		if (client == null)
			throw new NullPointerException("Parameter client is null");
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
					if (getLen == -1) return null;
				} catch (Exception e) {
					throw new LYException(e);
				}
				if (getLen == 0)
					throw new LYException("Impossible");
				// validate if receive finished
				if (transfer.getProtocol().validate(buffer, bufferLen) > 0)
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
				Protocol protocol = transfer.getProtocol();
				if (protocol == null)
					return true;
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
