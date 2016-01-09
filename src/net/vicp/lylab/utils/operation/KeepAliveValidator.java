package net.vicp.lylab.utils.operation;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AdditionalOperate;
import net.vicp.lylab.core.interfaces.KeepAlive;

public class KeepAliveValidator<T extends KeepAlive> extends NonCloneableBaseObject implements AdditionalOperate<T> {

	@Override
	public boolean operate(T item) {
		return item.keepAlive();
	}

}
