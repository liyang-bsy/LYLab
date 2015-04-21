package net.vicp.lylab.core.datastructure;

import net.vicp.lylab.core.CloneableBaseObject;

/**
 * 
 * @author liyang
 *
 * STL: “对”，可用于返回两个数据的时候用
 *
 */
public class Pair<T, K> extends CloneableBaseObject {
	private T left;
	private K right;
	
	public Pair()
	{
		this.left = null;
		this.right = null;
	}
	public Pair(T left, K right)
	{
		this.left =left;
		this.right = right;
	}
	public T getLeft() {
		return left;
	}
	public void setLeft(T left) {
		this.left = left;
	}
	public K getRight() {
		return right;
	}
	public void setRight(K right) {
		this.right = right;
	}

}
