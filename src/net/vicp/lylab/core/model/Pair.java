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
	public String toString() {
		String sLeft = left != null ? left.toString() : "null";
		String sRight = right != null ? right.toString() : "null";
		return "(" + sLeft.toString() + ", " + sRight.toString() + ")";
	}

}
