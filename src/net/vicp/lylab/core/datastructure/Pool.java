package net.vicp.lylab.core.datastructure;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author liyang
 *
 * STL: “池”，自动管理的并发池
 *
 */
public interface Pool<R> extends Cloneable, Closeable {
	public void changeContainer(Map<Long, R> c);
	
	public void add(R t);
	public R accessNextOne();
	public R accessRandomOne();
	public List<R> accessMany(Integer amount);
	public R remove(Long t);
	public boolean recycle(Long t);
}
