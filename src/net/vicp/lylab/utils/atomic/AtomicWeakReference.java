package net.vicp.lylab.utils.atomic;

import java.lang.ref.WeakReference;

public final class AtomicWeakReference<T> extends AtomicReference<T> {

	public AtomicWeakReference() {
		super();
	}

	public AtomicWeakReference(T ref) {
		super(new WeakReference<T>(ref));
	}

	public T get(Class<T> instanceClass)
	{
		if(value == null) createInstance(instanceClass, WeakReference.class);
		return value.get();
	}

}
