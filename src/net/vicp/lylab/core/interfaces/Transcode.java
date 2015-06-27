package net.vicp.lylab.core.interfaces;

import net.vicp.lylab.utils.internet.Protocol;

/**
 * 	General Executor interface for all classes.<br>
 * 	If run() was used.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface Transcode<T> {
	public Protocol encode();
	public T decode(Protocol protocol);
}
