package net.vicp.lylab.core.interfaces;

/**
 * Implement this means this object needs be invoked in specific interval.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public interface KeepAlive extends Initializable {
	public void setInterval(long interval);
	public boolean isAlive();
	public boolean isDying();
	public boolean keepAlive();
}
