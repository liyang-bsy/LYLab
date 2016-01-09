package net.vicp.lylab.core.interfaces;

/**
 * AdditionalOp interface for AutoGeneratePool.<br>
 * Once this was offered to pool, every validate will do additional operation before using a generated item<br>
 * <br><br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */

public interface AdditionalOperate<T> {
	/**
	 * Do some thing before use an item in AutoGeneratePool
	 * @param t
	 * @return
	 * true if it works fine
	 */
	public boolean operate(T item);
}
