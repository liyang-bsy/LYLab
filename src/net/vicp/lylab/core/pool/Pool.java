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

	public Long add(T t);
    public int size();
    public boolean isEmpty();
    public boolean isFull();
	public T remove(long objId);
	public T accessOne();
	public List<T> accessMany(int amount);
	public void clear();
    public void close();
    public boolean isClosed();
    public int getMaxSize();
    public void setMaxSize(int maxSize);
	
}
