package net.vicp.lylab.core;

public class NonCloneableBaseObject extends BaseObject implements Cloneable {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
