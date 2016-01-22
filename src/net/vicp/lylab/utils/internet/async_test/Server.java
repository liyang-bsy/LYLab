package net.vicp.lylab.utils.internet.async_test;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.server.aop.SimpleKeyDispatcherAop;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.internet.AsyncSocket;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.internet.transfer.Transfer;
import net.vicp.lylab.utils.tq.LYTaskQueue;

public class Server extends NonCloneableBaseObject implements LifeCycle {
	static Protocol p = new LYLabProtocol();
	static AsyncSocket as;
	
	public static void main(String[] args) throws Exception {
	}

	@Override
	public void initialize() {
		CoreDef.config = new Config("c:/config.txt");
		Transfer t =new Transfer();
		as = new AsyncSocket(8888, t, new SimpleHeartBeat());
		t.setTaskQueue((LYTaskQueue) CoreDef.config.getObject("LYTaskQueue"));
		t.initialize();
		
		Aop aop = new SimpleKeyDispatcherAop<Message>() {
			@Override
			protected BaseAction mapAction(Message request) {
				return new BaseAction() {
					@Override
					public void exec() {
						System.out.println("Action activated:\nreq:" + request + "\nres:" + response);
					}
				};
			}
		};
		aop.setProtocol((Protocol) CoreDef.config.getObject("protocol"));
		as.setAopLogic(aop);
		as.initialize();
		t.close();
	}

	@Override
	public void close() throws Exception {
as.close();

		
	}

}
