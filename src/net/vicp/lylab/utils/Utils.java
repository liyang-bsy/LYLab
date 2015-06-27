package net.vicp.lylab.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;

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
	
}
