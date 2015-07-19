package net.vicp.lylab.utils.internet.async;

import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.config.Config;
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
public abstract class BaseSocket extends Task implements InitializeConfig {
	private static final long serialVersionUID = 4493557570063372132L;
	
	// Some thing about this socket
	private boolean isServer;
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected String host;
	protected int port;
	protected Config config;
	protected static Aop aop;

	// Protocol
	protected Protocol protocol = null;

	// Callback below
	protected Callback beforeConnect = null;
	protected Callback afterClose = null;
	protected Callback beforeTransmission = null;
	protected Callback afterTransmission = null;
	
	public abstract byte[] doResponse(byte[] request);

	@Override
	public void obtainConfig(Config config) {
		this.config = config;
	}

	public boolean isServer() {
		return isServer;
	}

	protected void setIsServer(boolean isServer) {
		this.isServer = isServer;
	}
	public int getSocketMaxRetry() {
		return socketMaxRetry;
	}

	public void setSocketMaxRetry(int socketMaxRetry) {
		this.socketMaxRetry = socketMaxRetry;
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

	public static Aop getAop() {
		return aop;
	}

	public static void setAop(Aop aop) {
		BaseSocket.aop = aop;
	}

	public Callback getBeforeConnect() {
		return beforeConnect;
	}

	public void setBeforeConnect(Callback beforeConnect) {
		this.beforeConnect = beforeConnect;
	}

	public Callback getAfterClose() {
		return afterClose;
	}

	public void setAfterClose(Callback afterClose) {
		this.afterClose = afterClose;
	}

	public Callback getBeforeTransmission() {
		return beforeTransmission;
	}

	public void setBeforeTransmission(Callback beforeTransmission) {
		this.beforeTransmission = beforeTransmission;
	}

	public Callback getAfterTransmission() {
		return afterTransmission;
	}

	public void setAfterTransmission(Callback afterTransmission) {
		this.afterTransmission = afterTransmission;
	}

}
