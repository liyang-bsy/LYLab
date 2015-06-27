package net.vicp.lylab.utils.atomic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;

public final class AtomicStrongReference<T> extends AtomicObject<T> implements AutoInitialize<T> {

	public AtomicStrongReference() {
		super(null);
	}

	public AtomicStrongReference(T obj) {
		super(obj);
	}

    /**
     * Gets the current value.
     * A reference is read-only, but its referenced value can be modified.
     *
     * @return the current value
     */
	@Override
	public T get()
	{
		return value;
	}

	@Override
	public T get(Class<T> instanceClass, Object... constructorParameters)
	{
		if(value == null) createInstance(instanceClass, constructorParameters);
		return value;
	}
	
	protected void createInstance(Class<T> instanceClass, Object... constructorParameters) {
		synchronized (lock) {
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if (constructorParameters == null)
				throw new LYException("ConstructorParameters is null");
			if(value != null) return;
			try {
				List<Class<?>> list = new ArrayList<Class<?>>();
				for(Object param : constructorParameters)
					list.add(param.getClass());
				Class<?>[] classArray = new Class<?>[list.size()];
				list.toArray(classArray);
				Constructor<T> con = instanceClass.getDeclaredConstructor(classArray);
				value = con.newInstance(constructorParameters);
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
			return;
		}
	}


}
