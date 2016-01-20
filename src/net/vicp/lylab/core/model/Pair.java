package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

/**
 * 
 * @author liyang
 *
 *         SLL: “对”，可用于返回两个数据的时候用
 *
 */
public class Pair<L, R> extends CloneableBaseObject {
	private L left;
	private R right;

	public Pair() {
		this.left = null;
		this.right = null;
	}

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String sLeft = left != null ? left.toString() : "null";
		String sRight = right != null ? right.toString() : "null";
		return "(" + sLeft.toString() + ", " + sRight.toString() + ")";
	}

}
