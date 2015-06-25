package net.vicp.lylab.utils.atomic;

import java.lang.ref.SoftReference;

public final class AtomicSoftReference<T> extends AtomicReference<T> {

	public AtomicSoftReference() {
		this(null);
	}
	
	public AtomicSoftReference(SoftReference<T> ref) {
		super(ref);
	}
	
	public T createInstance(Class<T> instanceClass)
	{
		return super.createInstance(instanceClass, SoftReference.class);
	}
	
}
