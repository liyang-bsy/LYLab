package net.vicp.lylab.utils.internet.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
//import java.util.Arrays;







import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.tq.Task;

public class ClientRead extends Task {
	private Selector selector;
	private Aop aop;

	public ClientRead(Selector selector) {
		this.selector = selector;
		this.begin();
	}

	byte[] reserved = new byte[2000];
	int reservedLen = 0;

	ByteBuffer buffer = ByteBuffer.allocate(1024);

	Protocol protocol = new LYLabProtocol();
	
	private void reserve(byte[] last, int start, int len) {
		reservedLen = len - start;
		for (int i = start; i < len; i++) {
			reserved[i - start] = last[i];
		}
	}

	private byte[] useReserved(byte[] bb) {
		byte[] out = byteCat(reserved, 0, reservedLen, bb, 0, bb.length);
		reservedLen = 0;
		return out;
	}

	private byte[] byteCat(byte[] pre, int preOffset, int preCopyLenth, byte[] suf, int sufOffset, int sufCopyLenth) {
		byte[] out = new byte[preCopyLenth - preOffset - sufOffset + sufCopyLenth];
		int i;
		for (i = preOffset; i < preCopyLenth; i++)
			out[i - preOffset] = pre[i];
		for (int j = sufOffset; j < sufCopyLenth; j++)
			out[i - preOffset - sufOffset + j] = suf[j];
		return out;
	}

	private void receiveBasedAopDrive(SocketChannel socketChannel) throws IOException {
		if (socketChannel != null) {
			while(true)
			{
				buffer.clear();
				int len = socketChannel.read(buffer);
	
				len += reservedLen;
				byte[] ret = useReserved(buffer.array());
				int start = 0, next = 0;
				while ((next = protocol.validate(ret, start, len)) != 0) {
					// receive-based aop drive
					Message requestMsg = null;
					Message responseMsg = null;
					try {
						Object obj = protocol.decode(ret, start);
						// TODO
						if(obj instanceof HeartBeat) {
							send(socketChannel, ByteBuffer.wrap(protocol.encode(null))); //heartBeat);
						}
						requestMsg = (Message) obj;
					} catch (Exception e) {
						log.debug(Utils.getStringFromException(e));
					}
					if(requestMsg == null) {
						responseMsg = new Message();
						responseMsg.setCode(0x00001);
						responseMsg.setMessage("Message not found");
					}
					else
					{
						//TODO
						responseMsg = new Message();
						responseMsg.setMsgId(requestMsg.getMsgId());
						id++;
						//response = aop.doAction(null, msg);
					}
					byte[] response = (protocol == null ? null : protocol.encode(responseMsg));
					
					send(socketChannel, ByteBuffer.wrap(response));
					start = next;
				}
				if (start == len) {
					break;
				}
				reserve(ret, start, len);
			}
		}
	}
	int id=0;	//TODO
	private void send(SocketChannel socketChannel, ByteBuffer wrap) {
		// TODO Auto-generated method stub
		
	}

	public void exec() {
		try {
			while (selector.select() > 0) {
				// 遍历每个有可用IO操作Channel对应的SelectionKey
				for (SelectionKey sk : selector.selectedKeys()) {
					// 如果该SelectionKey对应的Channel中有可读的数据
					if (sk.isReadable()) {
						// 使用NIO读取Channel中的数据
						SocketChannel sc = (SocketChannel) sk.channel();
						receiveBasedAopDrive(sc);
					}
					// 删除正在处理的SelectionKey
					selector.selectedKeys().remove(sk);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("请求总数:"+id);
	}
}