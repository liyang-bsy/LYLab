package net.vicp.lylab.utils.internet.async_test;

import java.io.File;
import java.io.IOException;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.internet.AsyncSocket;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;

public class Client implements Callback {
	static Protocol protocol = new LYLabProtocol();
	static AsyncSocket as;
	
	public static AtomicInteger i = new AtomicInteger();

	public static void main(String[] args) throws IOException {
		CoreDef.config = new Config(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
		as = new AsyncSocket("localhost", 8888 , new SimpleHeartBeat());
		as.setAfterTransmission(new Client());
		as.initialize();
		
		try {
			SimpleHeartBeat msg = new SimpleHeartBeat();
			as.request(protocol.encode(msg));
			as.request(protocol.encode(msg));
			as.request(protocol.encode(msg));
			as.request(protocol.encode(msg));
			as.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(i.get());
	}

	@Override
	public void callback(Object... params) {
		i.incrementAndGet();
	}

}
