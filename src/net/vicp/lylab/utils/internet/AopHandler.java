package net.vicp.lylab.utils.internet;

import java.nio.channels.SocketChannel;

import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.tq.Task;

/**
 * Transfer will use AopHanlder to deal with client requests
 * @author Young
 *
 */
public class AopHandler extends Task {
	private static final long serialVersionUID = -8759689034880271599L;
	
	Pair<SocketChannel, byte[]> clientSession;
	AsyncSocket asyncSocket;
	
	public AopHandler(AsyncSocket asyncSocket, Pair<SocketChannel, byte[]> clientSession) {
		this.asyncSocket = asyncSocket;
		this.clientSession = clientSession;
	}
	
	@Override
	public void exec() {
		SocketChannel socketChannel = clientSession.getLeft();
		byte[] request = clientSession.getRight();
		byte[] response = null;
		Aop aop = asyncSocket.getAopLogic();
		if (aop != null)
			response = aop.doAction(socketChannel.socket(), request, 0);
		else
			response = request;
		asyncSocket.send(socketChannel, response);
	}

}
