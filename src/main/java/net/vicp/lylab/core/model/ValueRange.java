package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

/**
 * 数值范围工具类
 * 
 * @author liyang
 * @date 2017-03-01 17:01
 */
public class ValueRange<T extends Number> extends CloneableBaseObject {
	protected T from, to;

	public ValueRange() {
	}

	public ValueRange(T from, T to) {
		this.from = from;
		this.to = to;
	}

	public T getFrom() {
		return from;
	}

	public void setFrom(T from) {
		this.from = from;
	}

	public T getTo() {
		return to;
	}

	public void setTo(T to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ValueRange<T> other = (ValueRange<T>) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [from=" + from + ", to=" + to + "]";
	}

}
