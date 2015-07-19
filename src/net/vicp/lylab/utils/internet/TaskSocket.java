package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.async.BaseSocket;
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
public class TaskSocket extends BaseSocket implements Recyclable, Transmission {
	private static final long serialVersionUID = 883892527805494627L;
	
	// Raw data source
	protected Socket socket;
	
	protected InputStream in;
	protected OutputStream out;

	// Buffer
	private byte[] buffer = new byte[CoreDef.SOCKET_MAX_BUFFER];
	private int bufferLen = 0;
	protected Protocol protocol = null;

	public TaskSocket(ServerSocket serverSocket) {
		if(serverSocket == null) throw new LYException("Parameter serverSocket is null");
		try {
			this.socket = serverSocket.accept();
			this.host = socket.getInetAddress().getHostAddress();
			this.port = socket.getPort();
			in = socket.getInputStream();
			out = socket.getOutputStream();
			setIsServer(true);
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public TaskSocket(String host, Integer port) {
		this.host = host;
		this.port = port;
		setIsServer(false);
	}

	@Override
	public void exec() {
		try {
			if (isServer()) {
				byte[] bytes = receive();
				if(bytes == null)
					return;
				send(doResponse(bytes));
				close();
			} else {
				doRequest(null);
			}
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
		if(beforeTransmission != null)
			beforeTransmission.callback(request);
		if(isServer()) throw new LYException("Do request is forbidden to a server socket");
		byte[] ret = request(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
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
	
	@Override
	public void initialize() {
		// do nothing
	}
	
	public void connect()
	{
		if(isServer()) return;
		try {
			if(beforeConnect != null)
				beforeConnect.callback();
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}
	
	public boolean send(byte[] msg) {
		if(isClosed()) return false;
		try {
			out.write(msg);
			out.flush();
		} catch (Exception e) {
			throw new LYException("Send failed", e);
		}
		return true;
	}
	
	public byte[] receive() {
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
				
				if (protocol.validate(buffer, bufferLen) > 0)
					break;
			}
		}
		return buffer;
	}

	@Override
	public void close() {
		try {
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
		} catch (Exception e) {
			throw new LYException("Close failed", e);
		}
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed()
				|| in == null || out == null;
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

	public byte[] getBuffer() {
		return buffer;
	}

	public int getBufferLen() {
		return bufferLen;
	}

}
