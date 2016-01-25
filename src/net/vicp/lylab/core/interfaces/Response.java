package net.vicp.lylab.core.interfaces;

import java.net.Socket;

import net.vicp.lylab.core.model.Pair;

/**
 * General Transmission interface for all classes.<br>
 * Implements this means it could be used to communication with clients on Internet<br>
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Response {
	/**
	 * Receive a request and send a response
	 * @param 
	 * 		request bytes from client
	 * @return
	 * 		if success response to client
	 */
	Pair<byte[], Integer> receive(Socket client);

}
