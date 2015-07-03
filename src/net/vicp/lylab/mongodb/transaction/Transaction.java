package net.vicp.lylab.mongodb.transaction;

import java.io.Serializable;

import net.vicp.lylab.utils.tq.Task;

import org.mongodb.morphia.dao.BasicDAO;

public abstract class Transaction<T,K> extends Task implements Serializable {
	private static final long serialVersionUID = -1136872919552926090L;

	private BasicDAO<T,K> basicDao;
	private volatile Boolean reverted;
	
	public Transaction(BasicDAO<T,K> basicDao) {
		super();
		this.basicDao = basicDao;
		reverted = false;
	}
	
	public abstract Integer rollBack();
	
	public synchronized Boolean NotReverted()
	{
		return !reverted;
	}

	public BasicDAO<T,K> getBasicDao() {
		return basicDao;
	}

	public void setBasicDao(BasicDAO<T,K> basicDao) {
		this.basicDao = basicDao;
	}

	protected synchronized Boolean getReverted() {
		return reverted;
	}

	protected void setReverted(Boolean reverted) {
		this.reverted = reverted;
	}

}
