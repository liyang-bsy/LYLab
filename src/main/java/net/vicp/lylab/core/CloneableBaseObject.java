package net.vicp.lylab.core;

import net.vicp.lylab.core.exceptions.LYException;

public class CloneableBaseObject extends BaseObject implements Cloneable {
	@Override
	public BaseObject clone() {
		BaseObject obj;
		try {
			obj = (BaseObject) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new LYException("Clone not supported on this object", e);
		}
		// reset objectId
		obj.setObjectId(0);
		return obj;
	}
}
