package net.vicp.lylab.utils.atomic;

import net.vicp.lylab.core.exception.LYException;

public final class AtomicStrongReference<T> extends AtomicObject<T> {

	public AtomicStrongReference() {
		this(null);
	}
	
	public AtomicStrongReference(T obj) {
		super(obj);
	}
	
	public T createInstance(Class<T> instanceClass)
	{
		synchronized (lock) {
		if (value == null) {
			try {
				value = (T) instanceClass.newInstance();
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
		}}
		return (T) value;
	}

}
