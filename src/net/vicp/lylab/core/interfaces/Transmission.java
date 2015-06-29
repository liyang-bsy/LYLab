package net.vicp.lylab.core.interfaces;

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

public interface Transmission {
	/**
	 * Send a request and receive a response
	 * @param
	 * 		request bytes to server
	 * @return
	 * 		bytes from server
	 */
	public byte[] request(byte[] request);
	/**
	 * Receive a request and send a response
	 * @param 
	 * 		request bytes from client
	 * @return
	 * 		if success response to client
	 */
	public byte[] response(byte[] request);
	
}
