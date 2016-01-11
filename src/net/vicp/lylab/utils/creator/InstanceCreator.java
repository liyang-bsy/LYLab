package net.vicp.lylab.utils.creator;

public class InstanceCreator<T> extends AutoCreator<T> {

	public InstanceCreator(Class<T> instanceClass, Object... params) {
		super(instanceClass, params);
	}

	@Override
	public T newInstance() {
		return get();
	}

}
