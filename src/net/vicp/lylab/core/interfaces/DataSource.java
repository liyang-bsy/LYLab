package net.vicp.lylab.core.interfaces;

public interface DataSource<T> {
	public boolean running();
	public boolean hasNext();
	public T accessOne();
}
