package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Confirm;
import net.vicp.lylab.core.interfaces.Dispatcher;
import net.vicp.lylab.core.interfaces.HeartBeat;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Session;
import net.vicp.lylab.utils.tq.Task;

/**
 * A abstract session to be used for communicating with server. However you need
 * close session after using it. <br>
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2016.01.24
 * @version 1.0.0
 */
public abstract class AbstractSession extends Task implements Session {
	private static final long serialVersionUID = -8789772874306147807L;

	private boolean server;
	protected Protocol protocol;

	// for server mode
	protected Dispatcher<? super Confirm, ? super Confirm> dispatcher;
	// for long connection
	protected HeartBeat heartBeat;

	public AbstractSession(Protocol protocol, Dispatcher<? super Confirm, ? super Confirm> dispatcher,
			HeartBeat heartBeat) {
		super();
		if (protocol == null)
			throw new LYException("Parameter protocol is null");
		this.protocol = protocol;
		this.dispatcher = dispatcher;
		this.heartBeat = heartBeat;
	}

	public boolean isServer() {
		return server;
	}

	protected void setServer(boolean server) {
		this.server = server;
	}

}
