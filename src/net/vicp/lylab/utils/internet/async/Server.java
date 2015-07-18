package net.vicp.lylab.utils.internet.async;

import java.io.File;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.config.TreeConfig;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.internet.impl.Message;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;

public class Server {
	
	public static void main(String[] args) throws Exception {
		CoreDef.config = new TreeConfig(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
//		LYTimer.setConfig(CoreDef.config.getConfig("timer"));
		ProtocolUtils.setConfig(CoreDef.config.getConfig("protocol"));
		AsyncSocket as = new AsyncSocket(8888);
		as.begin("AsyncServer");
		Protocol p = new LYLabProtocol();
		Message  msg = new Message();
		msg.setMessage("new data");
		as.push("127.0.0.1", p.encode(msg));
	}

}
