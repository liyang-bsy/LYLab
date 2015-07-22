package net.vicp.lylab.core.pool;

import net.vicp.lylab.core.exception.LYException;
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

	public AutoGeneratePool(AutoGenerate<T> creator) {
		super();
		this.creator = creator;
	}

	public AutoGeneratePool(AutoGenerate<T> creator, long timeout, int maxSize) {
		super(timeout, maxSize);
		this.creator = creator;
	}

	protected T accessAndValidate() {
		T tmp = null;
		int attemptCount = 0;
		do {
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
					if (attemptCount > 10)
						throw new LYException("Attempt to add new instance to pool for 10 times, the container is full");
				}
				tmp = getFromAvailableContainer(id);
			}
		} while (false);
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

	@Override
	public void recycle() {
		synchronized (lock) {
			for (Long id : startTime.keySet()) {
				long start = startTime.get(id);
				if (System.currentTimeMillis() - start > timeout) {
					T tmp = busyContainer.get(id);
					if (tmp != null) {
						try {
							if (tmp instanceof AutoCloseable) {
								((AutoCloseable) tmp).close();
							}
						} catch (Exception e) {
							throw new LYException("Recycle failed", e);
						}
						busyContainer.remove(id);
						keyContainer.remove(id);
						startTime.remove(id);
					}
				}
			}
		}
		// recover();
		safeCheck();
	}

	// /**
	// * Refill destroyed sockets
	// */
	// public void recover()
	// {
	// while (maxSize > size()) {
	// int count = maxSize - size();
	// for (int i = 0; i < count; i++) {
	// add((T) user.refill());
	// }
	// }
	// }

}
