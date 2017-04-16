package net.vicp.lylab.core.interfaces;

import java.net.Socket;

/**
 * General Transmission interface for all classes.<br>
 * Implements this means it could be used to communication with servers on Internet<br>
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Request {
	/**
	 * Send a request and receive a response
	 * @param
	 * 		request bytes to server
	 * @return
	 * 		bytes from server
	 */
	void send(Socket client, byte[] request);
	
}
