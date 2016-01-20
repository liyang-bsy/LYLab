package net.vicp.lylab.utils.internet.async_test;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;

public class Server {
	static Protocol p = new LYLabProtocol();
	static AsyncSocket as;
	
	public static void main(String[] args) throws Exception {
		CoreDef.config = new Config("c:/config.txt");
		as = new AsyncSocket(8888, new SimpleHeartBeat());
		as.initialize();

		Message msg = new Message();
		for (int i = 0; i < 10002; i++) {
			msg.setUuid(""+i);
//			if(i%8000==0)
			{
//				System.out.println(i);
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			msg.setMessage("i=" + i);
			as.request("127.0.0.1", p.encode(msg));
		}
		try {
			Thread.sleep(200000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		as.request("127.0.0.1", p.encode(msg));
		as.close();
	}

}
