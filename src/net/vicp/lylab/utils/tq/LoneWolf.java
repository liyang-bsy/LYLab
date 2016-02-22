package net.vicp.lylab.utils.tq;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public abstract class LoneWolf extends Task {
	private static final long serialVersionUID = -8596789769206129576L;

	public LoneWolf() {
		setLonewolf(true);
	}
	
	@Override
	public BaseObject clone() {
		throw new LYException("Clone is not supported for a lone wolf");
	}
	
}
