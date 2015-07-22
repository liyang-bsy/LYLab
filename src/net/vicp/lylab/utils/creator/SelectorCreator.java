package net.vicp.lylab.utils.creator;

import java.nio.channels.Selector;

import net.vicp.lylab.core.exception.LYException;

public class SelectorCreator extends AutoGenerate<Selector> {

	public SelectorCreator(Class<Selector> instanceClass, Object[] params) {
		super(instanceClass, params);
	}

	@Override
	public Selector newInstance() throws LYException {
		try {
			return Selector.open();
		} catch (Exception e) {
			throw new LYException("Open selector failed", e);
		}
	}

}
