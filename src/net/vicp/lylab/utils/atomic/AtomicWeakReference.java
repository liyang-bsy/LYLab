package net.vicp.lylab.utils.atomic;

import java.lang.ref.WeakReference;

public final class AtomicWeakReference<T> extends AtomicReference<T> {

	public AtomicWeakReference() {
		this(null);
	}
	
	public AtomicWeakReference(WeakReference<T> ref) {
		super(ref);
	}
	
	public T createInstance(Class<T> instanceClass)
	{
		return super.createInstance(instanceClass, WeakReference.class);
	}
	
}
