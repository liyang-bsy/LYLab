package net.vicp.lylab.utils.internet.async;

import java.io.File;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.GlobalInitializer;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.config.Config;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.Task;

public class Server extends Task {
	private static final long serialVersionUID = -2775445482155105084L;
	
	static Protocol p = new LYLabProtocol();
	static AsyncSocket as;
	
	public static void main(String[] args) throws Exception {
		CoreDef.config = new Config(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
//		LYTimer.setConfig(CoreDef.config.getConfig("timer"));
		ProtocolUtils.setConfig(CoreDef.config.getConfig("protocol"));
		GlobalInitializer.createInstance(CoreDef.config.getConfig("init"), CoreDef.config);
		as = new AsyncSocket(8888, new SimpleHeartBeat());
		as.initialize();
		//selectorPool = new SelectorPool(CoreDef.DEFAULT_CONTAINER_TIMEOUT,CoreDef.DEFAULT_CONTAINER_MAX_SIZE);
//		as.begin("AsyncServer");
		new Server().begin();
	}

	@Override
	public void exec() {
		Message msg = new Message();
		for (int i = 0; i < 10002; i++) {
			msg.setMsgId(i);
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
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		as.close();
	}

}
