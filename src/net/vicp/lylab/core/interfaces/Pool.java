package net.vicp.lylab.core.interfaces;

/**
 * 
 * @author liyang
 *
 * 池的接口
 *
 */
public interface Pool<T> extends Cloneable, Iterable<T>, Recyclable {

	// Access
	public T access();
	
	// Storage
	
	// -> Add & Remove
	/**
	 * Add an Object returns its id
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public Long add(T t);

	/**
	 * Remove an Object returns itself
	 * @param target
	 * @return
	 * id(>0) if success, null if is full
	 */
	public T remove(long objId);
	
	// -> Information
	public int size();
	public boolean isEmpty();
	public void clear();
	
	// Size
	public int getMaxSize();
	public void setMaxSize(int maxSize);
	public boolean isFull();
	
}
