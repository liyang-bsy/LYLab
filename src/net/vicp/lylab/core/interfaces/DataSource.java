package net.vicp.lylab.core.interfaces;

public interface DataSource<T> {
	public boolean running();
	public boolean hasNext();
	public void takeback(Transcode t);
	public T accessOne();
	public int threadCountInc();
	public int threadCountDec();

}
