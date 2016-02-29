package net.vicp.lylab.core.pool.copy;

import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.AdditionalOperation;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.creator.AutoCreator;

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
public class AutoGeneratePool<T extends BaseObject> extends TimeoutRecyclePool<T> {
	AutoCreator<T> creator;
	AdditionalOperation<T> operator;

	public AutoGeneratePool(AutoCreator<T> creator) {
		super();
		this.creator = creator;
		this.operator = null;
	}

	public AutoGeneratePool(AutoCreator<T> creator, AdditionalOperation<T> operator) {
		super();
		this.creator = creator;
		this.operator = operator;
	}

	public AutoGeneratePool(AutoCreator<T> creator, AdditionalOperation<T> operator, long timeout, int maxSize) {
		super(timeout, maxSize);
		this.creator = creator;
		this.operator = operator;
	}

//	protected T accessAndValidate(T tmp) {
//		int attemptCount = 0;
//		 while (true) {
////			tmp = super.accessOne();
//			if (tmp == null) {
//				if (size() > maxSize)
//					return tmp;
//				Long id = null;
//				T passerby = null;
//				try {
//					passerby = creator.newInstance();
//				} catch (Throwable e) {
//					throw new LYException("Create new instance failed", e);
//				}
//				try {
//					if(passerby instanceof Initializable)
//						((Initializable) passerby).initialize();
//				} catch (Throwable e) {
//					throw new LYException("Initialize new instance failed", e);
//				}
//				while ((id = add(passerby)) == null) {
//					attemptCount++;
//					if (attemptCount > 10) {
//						Utils.tryClose(passerby);
//						throw new LYException("Attempt to add new instance to pool for 10 times, the container is full");
//					}
//				}
//				tmp = accessOne(id);
//			}
//			try {
//				if (operator == null || operator.operate(tmp))
//					break;
//			} catch (Exception e) {
//				log.error("Validate available failed:" + Utils.getStringFromException(e));
//			}
//			forceRemove(tmp.getObjectId());
//			Utils.tryClose(tmp);
//			continue;
//		}
//		return tmp;
//	}

	/**
	 * 
	 * @return
	 * true if success, false if failed.
	 */
	protected boolean createAndValidateAndAdd() {
		if (isFull())
			return false;
		int attempt = 0;
		while (attempt < 10) {
			attempt++;
			T passerby = null;
			try {
				// Create
				passerby = creator.newInstance();
				try {
					// Initialize
					if (passerby instanceof Initializable)
						((Initializable) passerby).initialize();
				} catch (Throwable t) {
					throw new LYException("Initialize new instance failed", t);
				}
				// Validate
				if(!validate(passerby))
					throw new LYException("Validation reported failed");
				// Add to Pool;
				if(add(passerby) == null)
					throw new LYException("Add to pool failed, maybe its full?");
			} catch (Throwable t) {
				Utils.tryClose(passerby);
				log.error("Create new instance failed" + Utils.getStringFromThrowable(t));
				continue;
			}
			return true;
		}
		throw new LYException("Create new instance failed, retried for too many times, lookup failure reasons from log.");
	}
	
	protected boolean validate(T target) {
		if (target == null)
			return false;
		if (operator == null)
			return true;
		try {
			return operator.doOperate(target);
		} catch (Exception e) {
			log.error("Validation failed:" + Utils.getStringFromException(e));;
			return false;
		}
	}

//	@Override
//	public T accessOne(boolean available) {
//		safeCheck();
//		synchronized (lock) {
//			T tmp = null;
//			if (available)
//				tmp = accessAndValidate();
//			else
//				tmp = getFromBusyContainer();
//			return tmp;
//		}
//	}
	
	@Override
	public T accessOne() {
		synchronized (lock) {
			if (availableSize() == 0 && isFull()) {
				// if full, recycle bad items and retry if is really full
				recycle();
				if (availableSize() == 0 && isFull())
					return null;
			}
			// Create and validate
			Iterator<Long> iterator = availableKeySet().iterator();
			if (!iterator.hasNext()) {
				if (!createAndValidateAndAdd())
					return null;
				iterator = availableKeySet().iterator();
			}
			return accessOne(iterator.next());
		}
	}

	@Override
	public T accessOne(long objId) {
		synchronized (lock) {
			safeCheck();
			if (availableSize() == 0)
				return null;
			T tmp = removeFromContainer(objId);
			if (tmp != null) {
				busyContainer.put(objId, tmp);
				startTime.put(objId, System.currentTimeMillis());
			}
			return tmp;
		}
	}
	
//	@Override
//	public T accessOne(long objId) {
//		synchronized (lock) {
//			safeCheck();
//			T tmp = null;
//			int attempt = 0;
//			do {
//				if(attempt > 10)
//					throw new LYException("Access item failed! Attempt for too many times!");
//				tmp = removeFromContainer(objId);
//				if (tmp != null) {
//					busyContainer.put(objId, tmp);
//					startTime.put(objId, System.currentTimeMillis());
//				}
//				attempt++;
//			} while (!validate(tmp));
//			return tmp;
//		}
//	}
	
	@Override
	public List<T> accessMany(int amount, boolean t) {
		throw new LYException("AccessMany() is not supported!");
	}

	public AutoCreator<T> getCreator() {
		return creator;
	}

	public void setCreator(AutoCreator<T> creator) {
		this.creator = creator;
	}

	public AdditionalOperation<T> getOperator() {
		return operator;
	}

	public void setOperator(AdditionalOperation<T> operator) {
		this.operator = operator;
	}

}
