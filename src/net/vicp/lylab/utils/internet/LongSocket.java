package net.vicp.lylab.utils.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.ByteUtils;

public abstract class LongSocket extends LYSocket {
	private static final long serialVersionUID = 5660256830220815074L;
	
	protected InputStream in;
	protected OutputStream out;

//	public static void main(String[] args) throws Exception {
//		System.out.println("这是Client");
//		LongSocketClient client = new LongSocketClient("127.0.0.1", 52041);
//		client.begin();
//		System.out.println(client);
//	}
	
	public LongSocket(ServerSocket serverSocket) {
		super(serverSocket);
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public LongSocket(String ip, int port) {
		super(ip, port);
		recycle(ip, port);
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
	
	@Override
	public boolean send(byte[] msg) throws IOException {
		out.write(msg);
		out.flush();
		return true;
	}

	@Override
	public byte[] receive() throws Exception {
		List<Byte> container = new ArrayList<Byte>();
		if (in != null) {
			byte[] rc = new byte[CoreDef.SOCKET_MAX_BUFFER];
			int totalRecv = 0, getLen = 0;
			while (true) {
				try {
					getLen = in.read(rc, 0, CoreDef.SOCKET_MAX_BUFFER);
				} catch (Exception e) {
					throw new LYException("Lost connection");
				}
				if(getLen == -1)
					break;
				if(getLen == 0)
					throw new LYException("I don't know...");
				totalRecv += getLen;
				container.addAll(ByteUtils.moveBytesToContainer(rc));
				int result = Protocol.validate(ByteUtils.copyBytesFromContainer(container), totalRecv);
				if(result == -1)
					throw new LYException("Bad data package");
				if(result == 0)
					break;
				if(result == 1)
					continue;
			}
		}
		return ByteUtils.copyBytesFromContainer(container);
	}

	@Override
	public boolean isClosed() {
		return socket == null || socket.isClosed()
				|| in == null || out == null;
	}

	@Override
	public void close() throws Exception {
		if (in != null) {
			in.close();
			in = null;
		}
		if (out != null) {
			out.close();
			out = null;
		}
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	@Override
	protected void recycle(String ip, int port) {
		try {
			close();
			socket = new Socket(ip, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}

}
