package net.vicp.lylab.mongodb.transaction;

import org.mongodb.morphia.dao.BasicDAO;

public class AddTransaction<T,K> extends Transaction<T,K> {

	private T bb;
	
	public AddTransaction(BasicDAO<T,K> basicDao, T T) {
		super(basicDao);
		this.bb = T;
	}
	
	@Override
	public void run() {
		getBasicDao().save(bb);
	}

	@Override
	public Integer rollBack()
	{
		setReverted(true);
		Integer n = getBasicDao().delete(bb).getN();
		return n;
	}

}
