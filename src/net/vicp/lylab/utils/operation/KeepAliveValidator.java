package net.vicp.lylab.utils.operation;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.AdditionalOperation;
import net.vicp.lylab.core.interfaces.KeepAlive;

public class KeepAliveValidator<T extends KeepAlive> extends NonCloneableBaseObject implements AdditionalOperation<T> {

	@Override
	public boolean doOperate(T item) {
		return item.keepAlive();
	}

}
