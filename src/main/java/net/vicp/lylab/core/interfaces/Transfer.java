package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.core.model.InetAddr;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public interface Transfer extends Initializable, Recyclable {

	public void putRequest(InetAddr clientAddr, byte[] buffer, int bufferLen);

	public void setSession(Session session);

	// getters & setters
	public Protocol getProtocol();

}
