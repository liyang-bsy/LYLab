package net.vicp.lylab.utils.atomic;

public final class AtomicBoolean extends AtomicObject<Boolean> {

	public AtomicBoolean() {
		super(false);
	}
	
	public AtomicBoolean(boolean t) {
		super(t);
	}
	
    /**
     * Gets the current read-only value.
     *
     * @return the current value
     */
	@Override
	public Boolean get()
	{
		return value.booleanValue();
	}
	
}
