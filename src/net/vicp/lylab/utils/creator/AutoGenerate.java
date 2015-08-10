package net.vicp.lylab.utils.creator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;

public abstract class AutoGenerate<T> extends CloneableBaseObject implements AutoInitialize<T> {
	Class<T> instanceClass;
	Object[] params;

	public AutoGenerate(Class<T> instanceClass, Object... params) {
		this.instanceClass = instanceClass;
		this.params = params;
	}

	/**
	 *  Simply speaking, you may try this:
	 *  <br>public T newInstance() {
	 *  <br>&nbsp;&nbsp;&nbsp;&nbsp;return get();
	 *  <br>}
	 * @return
	 * new instance to be create
	 * @throws LYException contains message about why create failed
	 */
	public abstract T newInstance();

	@SuppressWarnings("unchecked")
	public T get() {
		synchronized (lock) {
			if (instanceClass == null)
				throw new LYException("instanceClass is null");
			if (params == null)
				throw new LYException("params is null");
			try {
				List<Class<?>> list = new ArrayList<Class<?>>();
				for (Object param : params)
					list.add(param.getClass());
				Class<?>[] classArray = new Class<?>[list.size()];
				list.toArray(classArray);
				Constructor<T> con = null;
				Constructor<?>[] constructors = instanceClass.getDeclaredConstructors();
				for(Constructor<?> constructor:constructors) {
					Class<?>[] classes = constructor.getParameterTypes();
					if(classes.length != classArray.length) continue;
					boolean accessible = true;
					for (int i = 0; i < classes.length; i++)
						if (!classes[i].isAssignableFrom(classArray[i])) {
							accessible = false;
							break;
						}
					if(accessible)
						con = (Constructor<T>) constructor;
				}
				T value = con.newInstance(params);
				return value;
			} catch (Exception e) {
				throw new LYException("Can not create referenced object", e);
			}
		}
	}

	@Override
	@Deprecated
	public T get(Class<T> instanceClass, Object... constructorParameters) {
		return get();
	}

	public Class<T> getInstanceClass() {
		return instanceClass;
	}

	public void setInstanceClass(Class<T> instanceClass) {
		this.instanceClass = instanceClass;
	}

	public void setParams(Object... params) {
		this.params = params;
	}
	
}
