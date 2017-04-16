package net.vicp.lylab.core.pool;

import java.util.List;

/**
 * 
 * @author liyang
 *
 * 池的接口
 *
 */
public interface Pool<T> extends Cloneable, Iterable<T> {
	/**
	 * Add an Object returns its id
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public Long add(T t);
	
	public int size();
	public boolean isEmpty();
	public boolean isFull();

	/**
	 * Remove an Object returns itself
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public T remove(long objId);
	public T accessOne();
	public List<T> accessMany(int amount);
	public List<T> accessMany(int amount, boolean absolute);
	public void clear();
//	public void close();
//	public boolean isClosed();
	public int getMaxSize();
	public void setMaxSize(int maxSize);
	
}
