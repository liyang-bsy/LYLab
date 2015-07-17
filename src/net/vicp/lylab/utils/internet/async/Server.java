package net.vicp.lylab.utils.internet.async;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.config.TreeConfig;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

public class Server {
	
	protected Selector selector;
	protected Charset charset = Charset.forName("UTF-8");
	protected CharsetEncoder charsetEncoder = charset.newEncoder();
	protected CharsetDecoder charsetDecoder = charset.newDecoder();
	int count = 1;
	
	/**
	 * @throws Exception
	 */
	public Server() throws Exception{
		this(8888);
	}
	
	/**
	 * @param port
	 * @throws Exception
	 */
	public Server(int port) throws Exception {
		selector = Selector.open();
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress(port)); // port
		ssc.configureBlocking(false);
		ssc.register(selector, SelectionKey.OP_ACCEPT);// register

		while (true) {
			// selector 线程。select() 会阻塞，直到有客户端连接，或者有消息读入
			selector.select();
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove(); // 删除此消息
				// 并在当前线程内处理
				handleSelectionKey(selectionKey);
			}
		}

	}

	/**
	 * @param selectionKey
	 * @throws Exception
	 */
	public void handleSelectionKey(SelectionKey selectionKey) throws Exception {
		if (selectionKey.isAcceptable()) {
			ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
			SocketChannel socketChannel = ssc.accept();
			socketChannel.configureBlocking(false);
			Socket socket = socketChannel.socket();
			// 立即注册一个 OP_READ 的SelectionKey, 接收客户端的消息
			SelectionKey key = socketChannel.register(selector,SelectionKey.OP_READ);
			
			SocketAddress clientInfo = socket.getRemoteSocketAddress();
			key.attach("第 " + (count++) + " 个客户端 [" + clientInfo + "]");
			// 打印
			println(key.attachment() + " 连接成功");

		} else if (selectionKey.isReadable()) {

			// 有消息进来
			ByteBuffer byteBuffer = ByteBuffer.allocate(100);
			SocketChannel sc = (SocketChannel) selectionKey.channel();

			try {
				int len = sc.read(byteBuffer);
				// 如果len>0，表示有输入。如果len==0, 表示输入结束。需要关闭 socketChannel
				if (len > 0) {
					byteBuffer.flip();
					String msg = charsetDecoder.decode(byteBuffer).toString();
					println(selectionKey.attachment() + " ：" + msg);
					
					// 根据客户端的消息，查找到对应的输出
					String newMsg = "****************";
					ByteBuffer bt = charsetEncoder.encode(CharBuffer.wrap(newMsg + "\n"));
					sc.write(bt);
				} else {
					// 输入结束，关闭 socketChannel
					println(selectionKey.attachment()+ " 已关闭连接");
					sc.close();
				}

			} catch (Exception e) {
				// 如果read抛出异常，表示连接异常中断，需要关闭 socketChannel
				e.printStackTrace();
				sc.close();
			}
		} else if (selectionKey.isWritable()) {
			println("TODO: isWritable()");
		} else if (selectionKey.isConnectable()) {
			println("TODO: isConnectable()");
		} else {
			println("TODO: else");
		}

	}

	/**
	 * @param object
	 */
	public static void println(Object object) {
		System.out.println(object);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CoreDef.config = new TreeConfig(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
//		LYTimer.setConfig(CoreDef.config.getConfig("timer"));
		ProtocolUtils.setConfig(CoreDef.config.getConfig("protocol"));
		new AsyncSocket(8888).begin("AsyncServer");
	}

}
