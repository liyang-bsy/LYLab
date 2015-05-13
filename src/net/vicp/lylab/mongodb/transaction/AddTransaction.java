package net.vicp.lylab.mongodb.transaction;

import java.io.Serializable;

import org.mongodb.morphia.dao.BasicDAO;

public class AddTransaction<T,K> extends Transaction<T,K> implements Serializable {
	private static final long serialVersionUID = -7535913751420832948L;
	
	private T bb;
	
	public AddTransaction(BasicDAO<T,K> basicDao, T T) {
		super(basicDao);
		this.bb = T;
	}
	
	@Override
	public void exec() {
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
