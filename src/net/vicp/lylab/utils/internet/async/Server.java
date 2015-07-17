package net.vicp.lylab.utils.internet.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;

public class Server {
	// 服务器端通道
	ServerSocketChannel ssc;

	public void start() {
		try {
			// 异步IO的核心对象名selector 具有事件侦听的效果
			// selector就是您注册对各种io事件的兴趣的地方 而且当那些事件发生时 就是这个对象告诉您所发生的事情
			Selector selector = Selector.open();
			// 打开一个serversocketchannel通道
			ServerSocketChannel ssc = ServerSocketChannel.open();
			// 设为异步
			ssc.configureBlocking(false);
			// 绑定端口
			ServerSocket ss = ssc.socket();
			InetSocketAddress address = new InetSocketAddress(5555);
			ss.bind(address);
			// 注册事件 regisiter的第一个参数总是selector 第二个总是op_accept 这里他指定我们要监听accept事件
			// 也就是当有新的链接进来是发生的事件
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("端口注册完成");
			while (true) {
				// select()这个方法会阻塞 直到有一个已注册的事件发生 当一个或者更多的事件注册进来的时候 这个会返回事件的数量
				selector.select();
				// 调用selectedKeys()会返回事件对象集合
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				// 然后我们迭代处理每一个事件
				Iterator<SelectionKey> iter = selectionKeys.iterator();
				byte[] buffer = new byte[2048];
				ByteBuffer bb = ByteBuffer.allocate(2048);
				Protocol p = new LYLabProtocol();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					// 判断事件类型
					if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
						ServerSocketChannel nssc = (ServerSocketChannel) key
								.channel();
						sc = nssc.accept();
						// 设为非阻塞
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ);
						iter.remove();
						System.out.println("有新的链接" + sc);
					} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
						sc = (SocketChannel) key.channel();
						{
							while (true) {
								bb.clear();
								int a = sc.read(bb);
								buffer = bb.array();
								if (a == -1)
									break;
								if (a > 0) {
									Object o = p.decode(buffer);
									sc.write(ByteBuffer.wrap(p.encode(o)));
									break;
								}
							}
						}

//						sc.close();
//						System.out.println("连接处理结束");
						iter.remove();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new Server().start();
	}
}