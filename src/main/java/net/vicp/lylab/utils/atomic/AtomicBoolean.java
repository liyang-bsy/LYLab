package net.vicp.lylab.utils.atomic;

public final class AtomicBoolean extends AtomicObject<Boolean> {

	public AtomicBoolean() {
		super(false);
	}

	public AtomicBoolean(boolean t) {
		super(t);
	}

}
