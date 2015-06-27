package net.vicp.lylab.utils.atomic;

import java.lang.ref.SoftReference;

public final class AtomicSoftReference<T> extends AtomicReference<T> {

	public AtomicSoftReference() {
		super();
	}

	public AtomicSoftReference(T ref) {
		super(new SoftReference<T>(ref));
	}

	public T get(Class<T> instanceClass, Object... constructorParameters)
	{
		if(value == null)
			createInstance(instanceClass, SoftReference.class, constructorParameters);
		return value.get();
	}
	
}
