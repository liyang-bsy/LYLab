package net.vicp.lylab.core.interfaces;

import java.net.Socket;

import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.model.InetAddr;

/**
 * A session to be used for communicating with server.
 * Remember close the session after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2016.01.24
 * @version 1.0.0
 */
public interface Session extends LifeCycle, Transmission {

	public boolean isServer();

	public Socket getClient(InetAddr clientAddr);

}
