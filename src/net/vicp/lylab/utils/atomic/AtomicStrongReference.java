package net.vicp.lylab.utils.atomic;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;

public final class AtomicStrongReference<T> extends AtomicObject<T> implements AutoInitialize<T> {

	public AtomicStrongReference() {
		super(null);
	}

	public AtomicStrongReference(T obj) {
		super(obj);
	}
	
	public T get(Class<T> instanceClass)
	{
		if(value == null) createInstance(instanceClass);
		return value;
	}
	
	protected void createInstance(Class<T> instanceClass) {
		synchronized (lock) {
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if(value != null) return;
			try {
				value = (T) instanceClass.newInstance();
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
			return;
		}
	}


}
