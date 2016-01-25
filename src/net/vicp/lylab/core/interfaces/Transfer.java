package net.vicp.lylab.core.interfaces;

import java.net.Socket;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public interface Transfer extends Initializable, Recyclable {

	public void putRequest(Socket client, byte[] buffer, int bufferLen);

	public byte[] getResponse(Socket client);
	
	public void setSession(Session session);

	// getters & setters
	public Protocol getProtocol();

}
