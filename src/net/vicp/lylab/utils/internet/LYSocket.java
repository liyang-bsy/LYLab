package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.utils.ByteUtils;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.tq.Task;

public abstract class LYSocket extends Task implements Recyclable, AutoCloseable, Transmission {
	private static final long serialVersionUID = 883892527805494627L;
	
	protected Socket socket;
	
	private boolean isServer;
	
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected String ip;
	protected int port;

	public LYSocket(ServerSocket serverSocket) {
		if(serverSocket == null) throw new LYException("Parameter serverSocket is null");
		try {
			this.socket = serverSocket.accept();
			isServer = true;
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public LYSocket(String ip, int port) {
		this.ip = ip;
		this.port = port;
		isServer = false;
	}
	
	@Override
	public void exec() {
		if(!isServer()) return;
		try {
			while(!response(receive()));
			return;
		} catch (Exception e) {
			throw new LYException("Why?", e);
		}
	}
	
	@Override
	public boolean hasMoreResponse() {
		return false;
	}

	@Override
	public byte[] request(byte[] request) {
		if(isServer()) return null;
		while (true) {
			try {
				send(request);
				return receive();
			} catch (Exception e) {
				recycle();
			}
		}
	}

	@Override
	public boolean hasMoreRequest() {
		return false;
	}
	
	public boolean send(byte[] msg) throws Exception {
		try (OutputStream out = socket.getOutputStream();) {
			out.write(msg);
			out.flush();
			return true;
		}
	}

	public byte[] receive() throws Exception {
		try (InputStream in = socket.getInputStream();) {
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
						break;
					if (getLen == 0)
						throw new LYException("Impossible");
					totalRecv += getLen;
					container.addAll(ByteUtils.moveBytesToContainer(rc));
					int result = Protocol.validate(
							ByteUtils.copyBytesFromContainer(container),
							totalRecv);
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
	}

	@Override
	public void close() throws Exception {
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed();
	}

	@Override
	public boolean isRecyclable() {
		return !isServer() && isClosed();
	}

	@Override
	public void recycle() {
		if (socketRetry.incrementAndGet() > socketMaxRetry)
			throw new LYException("Socket retried for too many times");
		recycle(ip, port);
	}

	protected void recycle(String ip, int port) {
		if(isServer()) return;
		try {
			close();
			this.socket = new Socket(ip, port);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}

	public int getSocketMaxRetry() {
		return socketMaxRetry;
	}

	public void setSocketMaxRetry(int socketMaxRetry) {
		this.socketMaxRetry = socketMaxRetry;
	}

	public String getIp() {
		return ip;
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

}
