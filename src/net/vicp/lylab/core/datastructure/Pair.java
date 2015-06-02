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
	private T key;
	private K value;
	
	public Pair()
	{
		this.key = null;
		this.value = null;
	}
	public Pair(T key, K value)
	{
		this.key =key;
		this.value = value;
	}
	public T getLeft() {
		return key;
	}
	public void setLeft(T key) {
		this.key = key;
	}
	public K getRight() {
		return value;
	}
	public void setRight(K value) {
		this.value = value;
	}

}
