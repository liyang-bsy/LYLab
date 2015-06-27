package net.vicp.lylab.utils.atomic;

import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;

public abstract class AtomicReference<T> extends AtomicObject<Reference<T>> implements AutoInitialize<T> {

	public AtomicReference() {
		super(null);
	}

	public AtomicReference(Reference<T> ref) {
		super(ref);
		if(ref.get() == null)
			throw new LYException("Reference to nothing");
	}

	public T get(Class<T> instanceClass, Object... constructorParameters)
	{
		throw new LYException("refClass is null");
	}
	
	@SuppressWarnings("unchecked")
	protected void createInstance(Class<T> instanceClass, Class<?> refClass, Object... constructorParameters) {
		synchronized (lock) {
			if (value != null && value.get() != null)
				return;
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if (refClass == null)
				throw new LYException("refClass is null");
			if (!Reference.class.isAssignableFrom(refClass))
				throw new LYException("refClass is not java.lang.ref.Reference");
			if (constructorParameters == null)
				throw new LYException("ConstructorParameters is null");
			try {
				List<Class<?>> list = new ArrayList<Class<?>>();
				for(Object param : constructorParameters)
					list.add(param.getClass());
				Class<?>[] classArray = new Class<?>[list.size()];
				list.toArray(classArray);
				Constructor<T> con = instanceClass.getDeclaredConstructor(classArray);
				T tmp = con.newInstance(constructorParameters);
				Constructor<?> conRef = refClass.getDeclaredConstructor(Object.class);
				value = (Reference<T>) conRef.newInstance(tmp);
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
		}
	}

}
