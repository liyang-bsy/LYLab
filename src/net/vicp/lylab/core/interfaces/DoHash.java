package net.vicp.lylab.core.interfaces;

/**
 * General DoHash interface for all classes.<br>
 *
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface DoHash {
	/**
	 * Give a key, returns its hash code
	 * @param key
	 * @return
	 */
	public int hash(String key);
}
