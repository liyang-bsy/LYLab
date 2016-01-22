package net.vicp.lylab.utils.internet.session;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.tq.Task;

/**
 * A abstract session to be used for communicating with server.
 * However you need close session after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public abstract class AbstractSession extends Task implements LifeCycle {
	private static final long serialVersionUID = 4493557570063372132L;
	
	// Some thing about this socket
	private boolean server;
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected String host;
	protected int port;
	protected Aop aopLogic;

	protected static Pair<String, Integer> getClientSession(Socket socket) {
		return new Pair<>(socket.getInetAddress().getHostAddress(), socket.getLocalPort());
	}
	
	public boolean isServer() {
		return server;
	}

	protected AbstractSession setServer(boolean server) {
		this.server = server;
		return this;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getSocketRetry() {
		return socketRetry.get();
	}

}
