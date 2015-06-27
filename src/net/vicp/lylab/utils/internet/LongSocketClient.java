package net.vicp.lylab.utils.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Recyclable;
import net.vicp.lylab.utils.tq.Task;

public class LongSocketClient extends Task implements Recyclable, AutoCloseable {
	private static final long serialVersionUID = 5660256830220815074L;
	
	private Socket socket;
	protected InputStream in;
	protected OutputStream out;
	
	protected String ip;
	protected int port;

	public static void main(String[] args) throws Exception {
		System.out.println("这是Client");
		LongSocketClient client = new LongSocketClient("127.0.0.1", 52041);
		client.begin();
		System.out.println(client);
	}
	
	public LongSocketClient(Socket socket) {
		if(socket == null) throw new LYException("Socket is null");
		try {
			this.socket = socket;
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public LongSocketClient(String ip, int port) {
		recycle(ip, port);
	}
	
	private void recycle(String ip, int port) {
		try {
			close();
			socket = new Socket(ip, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}

	public void exec(){
		while (true) {
			try {
				System.out.println("-发");
				MyData m = new MyData();
				m.setValue("来自Client");
				send(m.encode().toBytes());
				System.out.println("-发完");
				System.out.println("-收:" + Protocol.fromBytes(receive()));
				System.out.println("-等");
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				recycle();
			}
		}
	}

	public boolean send(byte[] msg) throws IOException {
		out.write(msg);
		out.flush();
		return true;
	}

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
				container.addAll(moveBytesToContainer(rc));
				int result = Protocol.validate(copyBytesFromContainer(container), totalRecv);
				if(result == -1)
					throw new LYException("Bad data package");
				if(result == 0)
					break;
				if(result == 1)
					continue;
			}
		}
		return copyBytesFromContainer(container);
	}

	private List<Byte> moveBytesToContainer(byte[] bytes) {
		List<Byte> container = new ArrayList<Byte>();
		for (int i = 0; i < bytes.length; i++) {
			container.add(bytes[i]);
			bytes[i] = 0;
		}
		return container;
	}
	
	private byte[] copyBytesFromContainer(List<Byte> container)
	{
		byte[] bytes = new byte[container.size()];
		for(int i=0;i<container.size();i++)
			bytes[i] = container.get(i);
		return bytes;
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
	public boolean isRecyclable() {
		return socket == null;
	}

	@Override
	public void recycle() {
		recycle(ip, port);
	}

}