package net.vicp.lylab.core.interfaces;

import java.net.Socket;

import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.utils.internet.Session;

/**
 * Transfer could combine data packet chips into a full packet
 * And provide packet to server Aop logic
 * @author Young
 *
 */
public interface Transfer extends Initializable, Recyclable {

	public void putRequest(Socket client, byte[] buffer, int bufferLen);

	public Pair<byte[], Integer> getRequest(Socket client);
	
	public void setSession(Session session);

	// getters & setters
	public Protocol getProtocol();

}
