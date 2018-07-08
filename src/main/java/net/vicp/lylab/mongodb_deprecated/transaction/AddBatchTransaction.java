package net.vicp.lylab.mongodb_deprecated.transaction;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.dao.BasicDAO;

public class AddBatchTransaction<T,K> extends Transaction<T,K> implements Serializable {
	private static final long serialVersionUID = -7120887231389017100L;
	
	private List<T> bbList;
	private Integer iterator;
	
	public AddBatchTransaction(BasicDAO<T, K> basicDAO, List<T> bbList) {
		super(basicDAO);
		this.bbList = bbList;
	}
	
	@Override
	public void exec() {
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
