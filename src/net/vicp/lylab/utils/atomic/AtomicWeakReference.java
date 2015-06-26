package net.vicp.lylab.utils.atomic;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public final class AtomicWeakReference<T> extends AtomicReference<T> {

	public AtomicWeakReference() {
		this(null);
	}

	public AtomicWeakReference(WeakReference<T> ref) {
		super(ref);
	}

	public T get(Class<T> instanceClass)
	{
		if(value == null) createInstance(instanceClass, SoftReference.class);
		return value.get();
	}

}
