package net.vicp.lylab.mongodb.transaction;

//import net.ebaolife.core.utils.BaseBean;

import org.mongodb.morphia.dao.BasicDAO;

/**
 * 注意，此操作不可回滚
 * @author liyang
 */
public class DeleteTransaction<T,K> extends Transaction<T,K> {

	private T bb;
//	private T bbPast;
	
	public DeleteTransaction(BasicDAO<T, K> basicDao, T t) {
		super(basicDao);
		this.bb = t;
	}
	
	@Override
	public void run() {
//		try {
//			bbPast = (T) ((BaseBean) bb).clone();
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//		}
		getBasicDao().delete(bb);
	}

	@Override
	public Integer rollBack()
	{
//		setReverted(true);
//		List<T> bbList = getBasicDao().createQuery().field("_id").equal(((BaseBean) bb).getId()).asList();
//		if(bbList.size()==0)
//			getBasicDao().save(bbPast);
		return 0;
	}

}
