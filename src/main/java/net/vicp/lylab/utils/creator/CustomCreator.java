package net.vicp.lylab.utils.creator;

import net.vicp.lylab.core.exceptions.LYException;

public abstract class CustomCreator<T> extends AutoCreator<T> {

	public CustomCreator() {
		super(null);
	}

	@Override
	@Deprecated
	public T get(Class<T> instanceClass, Object... constructorParameters) {
		throw new LYException("Get(Class<T> , Object... ) is not available for CustomCreator");
	}

}
