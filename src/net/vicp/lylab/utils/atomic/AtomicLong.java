package net.vicp.lylab.utils.atomic;

public final class AtomicLong extends AtomicObject<Long> {

	public AtomicLong() {
		super(0L);
	}
	
	public AtomicLong(long t) {
		super(t);
	}
	
    /**
     * Gets the current read-only value.
     *
     * @return the current value
     */
	@Override
	public Long get()
	{
		return value.longValue();
	}

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
	public Long getAndAdd(long delta) {
		synchronized (lock) {
			long current = get();
			value += delta;
			return current;
		}
	}

	/**
	 * Atomically decrements by one the current value.
	 *
	 * @return the updated value
	 */
	public Long decrementAndGet() {
		synchronized (lock) {
			value--;
			return value;
		}
	}
	
	/**
	 * Atomically increments by one the current value.
	 *
	 * @return the updated value
	 */
	public Long incrementAndGet() {
		synchronized (lock) {
			value++;
			return value;
		}
	}

}
