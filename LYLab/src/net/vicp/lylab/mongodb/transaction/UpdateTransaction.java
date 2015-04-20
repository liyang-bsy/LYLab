package net.vicp.lylab.mongodb.transaction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bson.types.ObjectId;

import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

/**
 * 注意，此操作不可回滚
 * @author liyang
 */

public class UpdateTransaction<T,K> extends Transaction<T,K> {

	private T bb;
	public UpdateTransaction(BasicDAO<T,K> basicDao, T T) {
		super(basicDao);
		this.bb = T;
	}
	
	@Override
	public void run(){
		ObjectId idKey = null;
		UpdateOperations<T> mods = getBasicDao().createUpdateOperations().disableValidation();
		Field[] fs =  bb.getClass().getDeclaredFields();
		try {
			for(Field f : fs){
				Annotation annotation = f.getAnnotation(org.mongodb.morphia.annotations.Id.class);
				//有该类型的注释存在
				if (annotation != null) {
					char oldChar = f.getName().charAt(0);
					char newChar = (oldChar + "").toUpperCase().charAt(0);
					String methodName = "get" + f.getName().replace(oldChar, newChar);
					
					Method method;
					method = bb.getClass().getMethod(methodName);
					idKey = (ObjectId) method.invoke(bb);
					if(idKey == null) throw new NullPointerException("ID is null");
					break;
				}
			}
			List<T> tList = getBasicDao().createQuery().field(Mapper.ID_KEY).equal(idKey).asList();
			if(tList.size() == 0)
			{
				log.error("This ID doesn't pair any entry:" + idKey);
				throw new NullPointerException("This ID doesn't pair any entry:" + idKey);
			}
			T old = tList.get(0);
			Method mList[] = old.getClass().getDeclaredMethods();
			for(Method mtd : mList) do {
				if(!mtd.getName().startsWith("get") || mtd.getParameterTypes().length != 0) break;
				if(!mtd.invoke(bb).equals(mtd.invoke(old))) break;
				String varName = mtd.getName().substring(3);
				char oldChar = mtd.getName().charAt(0);
				String realVarName = varName.replace(mtd.getName().charAt(0), (oldChar + "").toLowerCase().charAt(0));
				mods = mods.set(realVarName, mtd.invoke(old));
			} while(false);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		if(!getReverted())
		{
			UpdateResults ur = getBasicDao().update(getBasicDao().createQuery().field(Mapper.ID_KEY).equal(idKey),mods);
			if(!ur.getUpdatedExisting())
				log.info("Update:" + ur.getUpdatedCount() + "\nInsert:" + ur.getInsertedCount() + "\n");
		}

	}

	@Override
	public Integer rollBack()
	{
		setReverted(true);
//		Integer n = getBasicDao().delete(bb).getN();
		return 0;
	}
	
}
