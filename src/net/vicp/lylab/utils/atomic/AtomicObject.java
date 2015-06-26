package net.vicp.lylab.utils.atomic;

import net.vicp.lylab.core.CloneableBaseObject;

public class AtomicObject<T> extends CloneableBaseObject {
	protected volatile T value;

	public AtomicObject(T initValue) {
		value = initValue;
	}

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
	public T getAndSet(T newValue)
	{
		synchronized (lock) {
			T tmp = value;
			value = newValue;
			return tmp;
		}
	}

    /**
     * Gets the current value.
     *
     * @return the current value
     */
	public T get()
	{
		return value;
	}

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
	public void set(T newValue)
	{
		synchronized (lock) {
			value = newValue;
		}
	}

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
	public boolean compareAndSet(T expect, T update)
	{
		synchronized (lock) {
			if(!value.equals(expect))
				return false;
			value = update;
			return true;
		}
	}

}
