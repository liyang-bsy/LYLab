package net.vicp.lylab.mongodb.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mongodb.morphia.dao.BasicDAO;

public abstract class Transaction<T,K> implements Runnable {

	protected Log log = LogFactory.getLog(getClass());
	
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
