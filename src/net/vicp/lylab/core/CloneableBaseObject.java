package net.vicp.lylab.core;

public class CloneableBaseObject extends BaseObject implements Cloneable {
	@Override
	public BaseObject clone() throws CloneNotSupportedException {
		BaseObject obj = (BaseObject) super.clone();
		obj.setObjectId(null);
		return obj;
	}
}
