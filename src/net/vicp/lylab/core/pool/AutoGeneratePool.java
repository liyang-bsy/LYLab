package net.vicp.lylab.core.pool;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AdditionalOp;
import net.vicp.lylab.utils.creator.AutoGenerate;

/**
 * You can access items one by one from an auto-generate pool, if no more the
 * available items<br>
 * and this pool is not full, it will auto generate one for you , do not forget
 * recycle it after using. <br>
 * <br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class AutoGeneratePool<T> extends TimeoutRecyclePool<T> {
	AutoGenerate<T> creator;
	AdditionalOp<T> operator;

	public AutoGeneratePool(AutoGenerate<T> creator) {
		super();
		this.creator = creator;
		this.operator = null;
	}

	public AutoGeneratePool(AutoGenerate<T> creator, AdditionalOp<T> operator) {
		super();
		this.creator = creator;
		this.operator = operator;
	}

	public AutoGeneratePool(AutoGenerate<T> creator, AdditionalOp<T> operator, long timeout, int maxSize) {
		super(timeout, maxSize);
		this.creator = creator;
		this.operator = operator;
	}

	protected T accessAndValidate() {
		T tmp = null;
		int attemptCount = 0;
		 while (true) {
			tmp = getFromAvailableContainer();
			if (tmp == null) {
				if (size() > maxSize)
					return tmp;
				Long id = null;
				T passerby = null;
				try {
					passerby = creator.newInstance();
				} catch (Throwable e) {
					throw new LYException("Create new instance failed", e);
				}
				while ((id = add(passerby)) == null) {
					attemptCount++;
					if (attemptCount > 10) {
						if(passerby instanceof AutoCloseable)
							try {
								((AutoCloseable) passerby).close();
							} catch (Exception e) { }
						throw new LYException("Attempt to add new instance to pool for 10 times, the container is full");
					}
				}
				tmp = getFromAvailableContainer(id);
			}
			if(operator != null && !operator.operate(tmp))
				continue;
			break;
		}
		return tmp;
	}

	@Override
	public T accessOne(boolean available) {
		safeCheck();
		synchronized (lock) {
			T tmp = null;
			if (available)
				tmp = accessAndValidate();
			else
				tmp = getFromBusyContainer();
			return tmp;
		}
	}

}
