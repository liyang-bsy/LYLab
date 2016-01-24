package net.vicp.lylab.utils.internet;

import net.vicp.lylab.core.interfaces.LifeCycle;
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
public abstract class AbstractSession extends Task implements Session, LifeCycle {
	private static final long serialVersionUID = -8789772874306147807L;

	private boolean server;

	public boolean isServer() {
		return server;
	}

	protected void setServer(boolean server) {
		this.server = server;
	}

}
