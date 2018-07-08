package net.vicp.lylab.utils.creator;

import net.vicp.lylab.core.interfaces.Initializable;

public class InitializationCreator<T extends Initializable> extends AutoCreator<T> {

	public InitializationCreator(Class<T> instanceClass, Object... params) {
		super(instanceClass, params);
	}

	@Override
	public T newInstance() {
		T t = get();
		t.initialize();
		return t;
	}

}
