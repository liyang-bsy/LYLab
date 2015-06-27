package net.vicp.lylab.utils.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
				send("来自Client".getBytes("UTF-8"));
				System.out.println("-发完");
				System.out.println("-收:" + receive());

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

	public String receive() throws Exception {
		StringBuilder sb = new StringBuilder();
		if (in != null) {
			byte[] rc = new byte[CoreDef.SOCKET_MAX_BUFFER];
			int getLen = 0, len = 0;
//			while((len = in.read(rc, getLen, CoreDef.SOCKET_MAX_BUFFER - getLen)) > 0)
			do {
				len = in.read(rc, getLen, CoreDef.SOCKET_MAX_BUFFER - getLen);
				if(len < 0)
					break;
				sb.append(rc);
				if(getLen == CoreDef.SOCKET_MAX_BUFFER)
					clearBytes(rc);
				getLen += len;
				String s = new String(rc, "UTF-8");
				sb.append(s);
			} while (false);
			sb.append(rc);
		}
		return sb.toString();
	}
	
	private void clearBytes(byte[] bytes)
	{
		for(int i=0;i<bytes.length;i++) bytes[i] = 0;
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