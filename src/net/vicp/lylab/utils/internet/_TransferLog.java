package net.vicp.lylab.utils.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class _TransferLog extends Thread {
	int max_byte = 1024 * 1024 * 2;
	byte[] buffer;
	private Socket socket = null;
	private InputStream read = null;
	private OutputStream write = null;
	private String Serverhost;
	private int Serverport;

	public _TransferLog(String host, int port) throws UnknownHostException, IOException {
		Serverhost = host;
		Serverport = port;
		buffer = new byte[max_byte];
		socket = new Socket(Serverhost, Serverport);
		socket.setSoTimeout(20000);
		read = socket.getInputStream();
		write = socket.getOutputStream();
	}

	// 关闭连接
	private void closesocket() {
		try {
			if (socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
		} catch (Exception e) {

		} finally {
			read = null;
			write = null;
			socket = null;
		}
	}
	
	// 发起连接
	public int totalLen() {
		return 0;
	}

	public boolean send(String data) {
		boolean ret = false;
		try {
			// 将json字符串做成自定义协议的结构
			write.write(data.getBytes("UTF-8"));
			int getlen = 0;
			// tcp协议下的读取循环
			while (true) {
				int len = read.read(buffer, getlen, max_byte - getlen);
				if (len <= 0) {
					//超过缓冲区?连接中断?
					break;
				} else {
					getlen += len;
					if (getlen == totalLen()) {
						return ret;
					}
				}
			}
			if (ret == false) {
				//连接中断
				closesocket();
			}
		} catch (Exception e) {
			closesocket();
		}
		return ret;
	}

	@Override
	public void run() {
//		try {
//			while (LogClient.isRunning) {
//				// 试着或取一条日志，如果不为空，即有数据
//				SocketJsonSend sjs = LogClient.getLog();
//				if (sjs != null) {
//					boolean ret = sendLog(sjs);
//					// 如果发送失败，则加回需要上传日志的容器头部
//					if (!ret)
//						LogClient.addLogAtHead(sjs);
//				} else
//					Thread.sleep(1000);
//			}
//		} catch (Throwable e) {
//		} finally {
//			synchronized (LogClient.threadCount) {
//				LogClient.threadCount--;
//			}
//		}
	}
}
