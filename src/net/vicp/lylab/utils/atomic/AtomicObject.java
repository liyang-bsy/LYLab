package net.vicp.lylab.utils.atomic;

import net.vicp.lylab.core.CloneableBaseObject;

public class AtomicObject<T> extends CloneableBaseObject {
	protected volatile T value;
	
	public AtomicObject(T initValue) {
		value = initValue;
	}
	
	public T getAndSet(T newValue)
	{
		synchronized (lock) {
			T tmp = value;
			value = newValue;
			return tmp;
		}
	}
	
	public T get()
	{
		return value;
	}

	public void set(T newValue)
	{
		synchronized (lock) {
			value = newValue;
		}
	}

	public boolean compareAndSet(T expect, T update)
	{
		synchronized (lock) {
			if(!value.equals(expect))
				return false;
			value = update;
			return true;
		}
	}
	
	public boolean weakCompareAndSet(T expect, T update)
	{
		if(!value.equals(expect))
			return false;
		value = update;
		return true;
	}

}
