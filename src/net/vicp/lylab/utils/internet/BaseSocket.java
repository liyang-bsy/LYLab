package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.server.aop.DefaultAop;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public abstract class BaseSocket extends Task {
	private static final long serialVersionUID = 4493557570063372132L;
	
	// Some thing about this socket
	private boolean isServer;
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected String host;
	protected int port;
	protected Aop aopLogic;

	// Protocol
	protected Protocol protocol = null;

	// Callback below
	protected Callback beforeConnect = null;
	protected Callback afterClose = null;
	protected Callback beforeTransmission = null;
	protected Callback afterTransmission = null;
	
	public boolean isServer() {
		return isServer;
	}

	protected BaseSocket setIsServer(boolean isServer) {
		this.isServer = isServer;
		return this;
	}
	public int getSocketMaxRetry() {
		return socketMaxRetry;
	}

	public BaseSocket setSocketMaxRetry(int socketMaxRetry) {
		this.socketMaxRetry = socketMaxRetry;
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

	public Callback getBeforeConnect() {
		return beforeConnect;
	}

	public BaseSocket setBeforeConnect(Callback beforeConnect) {
		this.beforeConnect = beforeConnect;
		return this;
	}

	public Callback getAfterClose() {
		return afterClose;
	}

	public BaseSocket setAfterClose(Callback afterClose) {
		this.afterClose = afterClose;
		return this;
	}

	public Callback getBeforeTransmission() {
		return beforeTransmission;
	}

	public BaseSocket setBeforeTransmission(Callback beforeTransmission) {
		this.beforeTransmission = beforeTransmission;
		return this;
	}

	public Callback getAfterTransmission() {
		return afterTransmission;
	}

	public BaseSocket setAfterTransmission(Callback afterTransmission) {
		this.afterTransmission = afterTransmission;
		return this;
	}

	public Aop getAopLogic() {
		if(aopLogic == null)
			synchronized (lock) {
				if (aopLogic == null) {
					aopLogic = new DefaultAop();
					aopLogic.initialize();
				}
			}
		return aopLogic;
	}

	public BaseSocket setAopLogic(Aop aopLogic) {
		this.aopLogic = aopLogic;
		return this;
	}

}
