package net.vicp.lylab.utils.creator;

import java.nio.channels.Selector;

import net.vicp.lylab.core.exception.LYException;

public class SelectorCreator extends CustomCreator<Selector> {

	@Override
	public Selector newInstance() {
		try {
			return Selector.open();
		} catch (Exception e) {
			throw new LYException("Open selector failed", e);
		}
	}

}
