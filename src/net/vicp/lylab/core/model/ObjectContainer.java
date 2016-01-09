package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

public class ObjectContainer<T> extends CloneableBaseObject {
	T object;

	public static <T> ObjectContainer<T> fromObject(T object) {
		if (object == null)
			throw new NullPointerException("Parameter object is null");
		return new ObjectContainer<T>(object);
	}
	
	private ObjectContainer(T object) {
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Object value = obj;
		if(obj instanceof ObjectContainer)
			value = ((ObjectContainer<?>) obj).getObject();
		if (object.getClass() != value.getClass())
			return false;
		if (!object.equals(value))
			return false;
		return true;
	}

}
