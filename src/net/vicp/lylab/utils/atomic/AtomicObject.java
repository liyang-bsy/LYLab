package net.vicp.lylab.utils.atomic;

import net.vicp.lylab.core.CloneableBaseObject;

public class AtomicObject<T> extends CloneableBaseObject {
	protected volatile T value;
	
	public AtomicObject()
	{
		value = null;
	}
	
	public AtomicObject(T t) {
		value = t;
	}
	
	public T getAndSet(T t)
	{
		synchronized (value) {
			T tmp = value;
			value = t;
			return tmp;
		}
	}
	
	public T get()
	{
		return value;
	}

	public void unsafeSet(T t)
	{
//		synchronized (value) {
//				while(!value.equals(t))
			value = t;
//		}
	}

	public void set(T t)
	{
		synchronized (value) {
				while(!value.equals(t))
			value = t;
		}
	}
	
}
