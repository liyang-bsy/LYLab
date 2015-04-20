package net.vicp.lylab.core;

public class UniqueBaseObject extends BaseObject implements Cloneable {

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	protected static Object self;
	
	public static Object getSelf() {
		return self;
	}

	public static void setSelf(Object self) {
		UniqueBaseObject.self = self;
	}

}
