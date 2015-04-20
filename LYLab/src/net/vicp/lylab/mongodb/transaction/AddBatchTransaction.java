package net.vicp.lylab.mongodb.transaction;

import java.util.List;

import org.mongodb.morphia.dao.BasicDAO;

public class AddBatchTransaction<T,K> extends Transaction<T,K> {

	private List<T> bbList;
	private Integer iterator;
	
	public AddBatchTransaction(BasicDAO<T, K> basicDAO, List<T> bbList) {
		super(basicDAO);
		this.bbList = bbList;
	}
	
	@Override
	public void run() {
		for(iterator=0 ; iterator<bbList.size() && NotReverted() ; iterator++)
			getBasicDao().save(bbList.get(iterator));
	}
	
	public Integer rollBack()
	{
		setReverted(true);
		Integer n = 0;
		for( ; iterator>=0 ; iterator--)
			n += getBasicDao().delete(bbList.get(0)).getN();
		return n;
	}

}
