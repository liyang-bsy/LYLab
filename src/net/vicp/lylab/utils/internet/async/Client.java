package net.vicp.lylab.utils.internet.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.SimpleMessage;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;

public class Client {
	Protocol protocol = new LYLabProtocol();

	// 信道选择器
	private Selector selector;
	// 与服务器通信的信道
	SocketChannel socketChannel;
	// 要连接的服务器Ip地址
	private String hostIp;
	// 要连接的远程服务器在监听的端口
	private int hostListenningPort;

	public Client(String HostIp, int HostListenningPort) throws IOException {
		this.hostIp = HostIp;
		this.hostListenningPort = HostListenningPort;

		initialize();
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		// 打开监听信道并设置为非阻塞模式
		socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
				hostListenningPort));
		socketChannel.configureBlocking(false);

		// 打开并注册选择器到信道
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 启动读取线程
		new ClientRead(selector);
	}

	/**
	 * 发送字符串到服务器
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMsg(byte[] message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer.wrap(message);
		socketChannel.write(writeBuffer);
	}

	public static void main(String[] args) throws IOException {
		Client client = new Client("localhost", 8888);
//
//		Protocol protocol= new LYLabProtocol();
//		SimpleMessage msg = new SimpleMessage();
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
	}
//	  
//    public void exec() {
//        try {
//            SocketAddress address = new InetSocketAddress("localhost", 8888);
//            SocketChannel client = SocketChannel.open(address);
//            client.configureBlocking(false);
//            Message msg = new Message();
//            ByteBuffer buffer = ByteBuffer.wrap(protocol.encode(msg));
//            int d = client.write(buffer);
//            System.out.println("发送数据("+d+"): " + new String(buffer.array()));
//            while (true) {
//                int i = client.read(buffer);
//                if (i > 0) {
//                    byte[] b = buffer.array();
//                    System.out.println("接收数据: " + new String(b));
//                    client.close();
//                    System.out.println("连接关闭!");
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    
//    public static void main(String args[]){
//    	LYTaskQueue tq = new LYTaskQueue();
//    	tq.start();
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
//        tq.addTask(new Client());
////        try {
////			tq.close();
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//    }
}
