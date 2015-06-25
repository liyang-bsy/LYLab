package net.vicp.lylab.utils.atomic;

public final class AtomicLong extends AtomicObject<Long> {

	public AtomicLong() {
		super(0L);
	}
	
	public AtomicLong(long t) {
		super(t);
	}
	
}
