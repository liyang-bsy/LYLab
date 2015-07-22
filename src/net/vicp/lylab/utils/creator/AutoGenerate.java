package net.vicp.lylab.utils.creator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;

public abstract class AutoGenerate<T> extends CloneableBaseObject implements
		AutoInitialize<T> {
	Class<T> instanceClass;
	Object[] params;

	public AutoGenerate(Class<T> instanceClass, Object[] params) {
		this.instanceClass = instanceClass;
		this.params = params;
	}

	/**
	 *  Simply speaking, you may try this:
	 *  <br>public T newInstance() {
	 *  <br>&nbsp;&nbsp;&nbsp;&nbsp;return get(instanceClass, params);
	 *  <br>}
	 * @return
	 * new instance to be create
	 * @throws LYException contains message about why create failed
	 */
	public abstract T newInstance() throws LYException;

	@Override
	@Deprecated
	public T get(Class<T> instanceClass, Object... constructorParameters) {
		synchronized (lock) {
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if (constructorParameters == null)
				throw new LYException("ConstructorParameters is null");
			try {
				List<Class<?>> list = new ArrayList<Class<?>>();
				for (Object param : constructorParameters)
					list.add(param.getClass());
				Class<?>[] classArray = new Class<?>[list.size()];
				list.toArray(classArray);
				Constructor<T> con = instanceClass.getDeclaredConstructor(classArray);
				T value = con.newInstance(constructorParameters);
				return value;
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
		}
	}
}
