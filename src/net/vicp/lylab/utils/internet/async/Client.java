package net.vicp.lylab.utils.internet.async;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.internet.impl.SimpleConfirm;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

public class Client extends Task {
	Protocol protocol = new LYLabProtocol();
    public void exec() {
        try {
            SocketAddress address = new InetSocketAddress("localhost", 5555);
            SocketChannel client = SocketChannel.open(address);
            client.configureBlocking(false);
            SimpleConfirm sc = new SimpleConfirm(-2);
            ByteBuffer buffer = ByteBuffer.allocate(20);
            //protocol.encode(sc);
            int d = client.write(buffer);
            System.out.println("发送数据: " + new String(buffer.array()));
            while (true) {
                int i = client.read(buffer);
                if (i > 0) {
                    byte[] b = buffer.array();
                    System.out.println("接收数据: " + new String(b));
                    client.close();
                    System.out.println("连接关闭!");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]){
    	LYTaskQueue tq = new LYTaskQueue();
    	tq.start();
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        tq.addTask(new Client());
        try {
			tq.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
