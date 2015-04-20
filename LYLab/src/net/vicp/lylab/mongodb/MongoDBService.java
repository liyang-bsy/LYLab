package net.vicp.lylab.mongodb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;

import net.vicp.lylab.core.datastructure.Pair;
import net.vicp.lylab.utils.Page;
import net.vicp.lylab.mongodb.transaction.AddBatchTransaction;
import net.vicp.lylab.mongodb.transaction.AddTransaction;
import net.vicp.lylab.mongodb.transaction.DeleteTransaction;
import net.vicp.lylab.mongodb.transaction.Transaction;
import net.vicp.lylab.mongodb.transaction.UpdateTransaction;

/**
 * MongoDBService for MongoDBOperation
 * @author		liyang
 * @version		0.1.2
 */
public class MongoDBService<T> {

	public final Class<T> clazz;
	private MongoDBDao<T> basicDao;
	
	public MongoDBService(Class<T> entityClass) {
		this.clazz = entityClass;
	}
	
	public MongoDBDao<T> getDao()
	{
		if(this.basicDao == null)
			this.basicDao = new MongoDBDao<T>(clazz);
		return basicDao;
	}
	/**
	 * 增加一个对象
	 * @param br	对象模型
	 */
	public Transaction<T, String> add(T br)
	{
		try {
			Transaction<T, String> t = new AddTransaction<T, String>(getDao(), br);
			new Thread(t).start();
//			TaskQueue.addTask(t);
			return t;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 增加一批对象
	 * @param brList	对象模型列表
	 */
	public Transaction<T, String> add(List<T> brList)
	{
		try {
			Transaction<T, String> t = new AddBatchTransaction<T, String>(getDao(), brList);
			new Thread(t).start();
			return t;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 删除一个对象
	 * @param br	对象模型
	 */
	public Transaction<T, String> delete(T br)
	{
		try {
			Transaction<T, String> t = new DeleteTransaction<T, String>(getDao(), br);
			new Thread(t).start();
			return t;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 修改一个对象
	 * @param br	对象模型
	 */
	public Transaction<T, String> update(T br)
	{
		try {
			Transaction<T, String> t = new UpdateTransaction<T, String>(getDao(), br);
			new Thread(t).start();
			return t;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	// queries below
	/**
	 * 获取查询Query自己做查询条件
	 */
	public Query<T> getQuery()
	{
		return getDao().createQuery().disableValidation();
	}
	/**
	 * 查询日志通用接口查询多个对象
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @throws SQLException
	 */
	public List<T> queryForList(String[] filter, Object[] value) throws SQLException
	{
		if(filter == null || value == null)
			return new ArrayList<T>();
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		return query.asList();
	}
	/**
	 * 查询日志通用接口查询多个对象
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @param order		排序关键字
	 * @throws SQLException
	 */
	public List<T> queryForList(String[] filter, Object[] value, String order) throws SQLException
	{
		if(filter == null || value == null)
			return new ArrayList<T>();
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		return query.order(order).asList();
	}
	/**
	 * 查询日志通用接口查询多个对象
	 * @param page		分页信息
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @param order		排序关键字
	 * @throws SQLException
	 */
	public List<T> queryForList(Page page, String[] filter, Object[] value, String order) throws SQLException
	{
		if(filter == null || value == null)
			return new ArrayList<T>();
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		page.setTotalProperty((int) query.countAll());
		return query.limit(page.getPageSize()).offset(page.getFirstResult()).order(order).asList();
	}
	/**
	 * 查询日志通用接口，查询多个对象的总数
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @throws SQLException
	 */
	public Long queryCount(String[] filter, Object[] value) throws SQLException
	{
		if(filter == null || value == null)
			return new Long(0);
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		return query.countAll();
	}
	/**
	 * 查询日志通用接口，但仅查一个对象
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @param order		排序条件
	 * @throws SQLException
	 */
	public T queryForObject(String[] filter, Object[] value, String order) throws SQLException
	{
		if(filter == null || value == null)
			return null;
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		List<T> tmpList = query.order(order).limit(1).asList();
		if(tmpList.size()==0)
			return null;
		else
			return tmpList.get(0);
	}
	/**
	 * 查询日志通用接口，但仅查一个对象<br>
	 * [!]该接口不排序，除非该条件约束下只有一个元素，否则不保证取到所希望获得的数据
	 * @param filter	条件字符串组
	 * @param value		参数组
	 * @throws SQLException
	 */
	public T queryForObject(String[] filter, Object[] value) throws SQLException
	{
		if(filter == null || value == null)
			return null;
		Query<T> query = getDao().createQuery().disableValidation();
		queryAnalasis(query, filter, value);
		List<T> tmpList = query.limit(1).asList();
		if(tmpList.size()==0)
			return null;
		else
			return tmpList.get(0);
	}
	// private method
	// 处理终止符号
	private void endSigal(Stack<Pair<String, Object>> cps, Query<T> q, List<Criteria> cl) throws SQLException
	{
		CriteriaContainer tmp = null;
		while(!cps.isEmpty() && !cps.peek().getLeft().contains("("))
			cl.add(makeCriteria(q, cps.pop()));
		if(cps.isEmpty())
		{
			tmp = q.and(cl.toArray(new CriteriaContainer[cl.size()]));
			cl.clear();
			cl.add(tmp);
			return;
		}
		String operator = cps.pop().getLeft();
		if(operator.equals("and("))
		{
			tmp = q.and(cl.toArray(new CriteriaContainer[cl.size()]));
			cl.clear();
			cl.add(tmp);
		}
		else if(operator.equals("or("))
		{
			tmp = q.or(cl.toArray(new CriteriaContainer[cl.size()]));
			cl.clear();
			cl.add(tmp);
		}
		else throw new SQLException();
	}
	// 制作一条Criteria
	private Criteria makeCriteria(Query<T> q, Pair<String, Object> pair)
	{
		if(pair == null) return null;
		if(pair.getRight().getClass().getName().equals("java.lang.String"))
		{
			String regSigList = "*.?+$^[](){}|/";
			pair.setRight(((String)pair.getRight()).replaceAll("\\\\", "\\\\\\\\"));
			for(char charRegSig:regSigList.toCharArray())
			{
				String regSig = String.valueOf(charRegSig);
				pair.setRight(((String)pair.getRight()).replaceAll("\\" + regSig, "\\\\" + regSig));
			}
		}
		if(pair.getLeft().trim().equals("")) return null;
		Criteria tmp = null;
		// 模糊搜索
		if(pair.getLeft().endsWith(" like")) tmp = q.criteria(pair.getLeft().replace(" like", "").trim()).contains(pair.getRight().toString());
		// 运算操作
		else if(pair.getLeft().contains(">="))
			tmp = q.criteria(pair.getLeft().replace(">=", "").trim()).greaterThanOrEq(pair.getRight());
		else if(pair.getLeft().contains("<="))
			tmp = q.criteria(pair.getLeft().replace("<=", "").trim()).lessThanOrEq(pair.getRight());
		else if(pair.getLeft().contains("!="))
			tmp = q.criteria(pair.getLeft().replace("!=", "").trim()).notEqual(pair.getRight());
		else if(pair.getLeft().contains(">"))
			tmp = q.criteria(pair.getLeft().replace(">", "").trim()).greaterThan(pair.getRight());
		else if(pair.getLeft().contains("<"))
			tmp = q.criteria(pair.getLeft().replace("<", "").trim()).lessThan(pair.getRight());
		else if(pair.getLeft().contains("="))
			tmp = q.criteria(pair.getLeft().replace("=", "").trim()).equal(pair.getRight());
		else if(pair.getLeft().endsWith(" in"))
			tmp = q.criteria(pair.getLeft().replaceFirst(" in$", "").trim()).in((Iterable<?>) pair.getRight());
		else if(pair.getLeft().endsWith(" nin"))
			tmp = q.criteria(pair.getLeft().replaceFirst(" nin$", "").trim()).in((Iterable<?>) pair.getRight());
		// 等值匹配
		else tmp = q.criteria(pair.getLeft()).equal(pair.getRight());
		return tmp;
	}
	// Query顺序分析
	private void queryAnalasis(Query<T> q,String[] k, Object[] v) throws SQLException
	{
		if(k == null || v == null) return;
		/* 0.普通模式 1.且模式 2.或模式 */
		Stack<Pair<String, Object>> cps = new Stack<Pair<String, Object>>();
		List<Criteria> cl = new ArrayList<Criteria>();
		// 符号分析
		for(Integer i = 0, j = 0; i < k.length ; i++)
		{
			k[i] = k[i].trim();
			// 且模式
			if(k[i].contains("(") || k[i].contains(")"))
			{
				String keyword = k[i].split("\\(")[0].trim();
				if(keyword.startsWith("and"))
				{
					cps.push(new Pair<String, Object>("and(", ""));
					String key = k[i].replace("and(", "").trim();
					if(key.endsWith(")")) throw new SQLException("\"(\" and \")\" exists in one condition");
					if(!key.equals(""))
					{
						cps.push(new Pair<String, Object>(key, v[j]));
						j++;
					}
				}
				// 或模式
				else if(keyword.startsWith("or"))
				{
					cps.push(new Pair<String, Object>("or(", ""));
					String key = k[i].replace("or(", "").trim();
					if(key.endsWith(")")) throw new SQLException();
					if(!key.equals(""))
					{
						if(!(v[j] == null || (v[j].getClass().getName().equals("java.lang.String") && ((String) v[j]).trim().equals(""))))
							cps.push(new Pair<String, Object>(key, v[j]));
						j++;
					}
				}
				// 结束符号
				else if(keyword.endsWith(")"))
				{
					String key = k[i].replace(")", "").trim();
					if(!key.equals(""))
					{
						if(!(v[j] == null || (v[j].getClass().getName().equals("java.lang.String") && ((String) v[j]).trim().equals(""))))
							cps.push(new Pair<String, Object>(key, v[j]));
						j++;
					}
					endSigal(cps, q, cl);
				}
			}
			// 普通模式
			else
			{
				cps.push(new Pair<String, Object>(k[i].trim(), v[j]));
				j++;
			}
		}
		while(!cps.isEmpty())
			endSigal(cps, q, cl);
	}
}
