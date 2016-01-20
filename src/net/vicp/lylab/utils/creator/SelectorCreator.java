package net.vicp.lylab.utils.creator;

import java.nio.channels.Selector;

import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.ObjectContainer;

public class SelectorCreator extends CustomCreator<ObjectContainer<Selector>> {

	@Override
	public ObjectContainer<Selector> newInstance() {
		try {
			return ObjectContainer.fromObject(Selector.open());
		} catch (Exception e) {
			throw new LYException("Open selector failed", e);
		}
	}

}
