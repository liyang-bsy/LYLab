package net.vicp.lylab.utils.atomic;

import java.lang.ref.Reference;
import java.lang.reflect.Constructor;

import net.vicp.lylab.core.exception.LYException;

public abstract class AtomicReference<T> extends AtomicObject<Reference<T>> {

	public AtomicReference() {
		this(null);
	}

	public AtomicReference(Reference<T> ref) {
		super(ref);
	}

	public T get(Class<T> instanceClass)
	{
		throw new LYException("refClass is null");
	}
	
	@SuppressWarnings("unchecked")
	protected void createInstance(Class<T> instanceClass, Class<?> refClass) {
		synchronized (lock) {
			if (value != null && value.get() != null)
				return;
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if (refClass == null)
				throw new LYException("refClass is null");
			if (!Reference.class.isAssignableFrom(refClass))
				throw new LYException("refClass is not java.lang.ref.Reference");
			try {
				T tmp = (T) instanceClass.newInstance();
				Constructor<?> con = refClass.getDeclaredConstructor(Object.class);
				value = (Reference<T>) con.newInstance(tmp);
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
		}
	}

}
