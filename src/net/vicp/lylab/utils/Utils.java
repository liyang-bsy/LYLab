package net.vicp.lylab.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;

public class Utils extends NonCloneableBaseObject {
	
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
	
	public static boolean inList(@SuppressWarnings("rawtypes") List list, Object item) {
		if(list == null || item == null) return false;
		for(Object o:list)
			if(o.equals(item))
				return true;
		return false;
	}

	/**
	 * 获取异常/错误的字符串内容
	 * @param t
	 * @return
	 */
	public static String getStringFromException(Throwable e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "\r\n" + sw.toString() + "\r\n";
		} catch (Exception ex) {
			return "bad getErrorInfoFromException";
		}
	}
//	
//	public static String toJson(Object obj)
//	{
//		return toJson(obj, new String[] { });
//	}
	
	public static String toJson(Object obj, String ... excludeRule)
	{
		if(obj == null)
			throw new LYException("Parameter obj is null");
		return new JSONSerializer().exclude("*.class", "*.objectId").exclude(excludeRule).deepSerialize(obj);
	}

	public static Object toObject(String json, String className)
	{
		if(json == null)
			throw new LYException("Parameter json is null");
		if(className == null)
			throw new LYException("Parameter className is null");
		try {
			return new JSONDeserializer<Object>().use(null, Class.forName(className)).deserialize(json);
		} catch (Exception e) {
			throw new LYException("Can not found class name[" + className + "]", e);
		}
	}

	public Object toObject(String json, Class<?> instanceClass)
	{
		if(json == null)
			throw new LYException("Parameter json is null");
		if(instanceClass == null)
			throw new LYException("Parameter instanceClass is null");
		return new JSONDeserializer<Object>().use(null, instanceClass).deserialize(json);
	}
	
}
