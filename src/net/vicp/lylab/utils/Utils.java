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
	
	@SuppressWarnings("rawtypes")
	public static boolean inList(List list, Object item) {
		for(Object o:list)
			if(o == item)
				return true;
		return false;
	}

	/**
	 * 获取异常/错误的字符串内容
	 * @param t
	 * @return
	 */
	public static String getStringFromException(Throwable t) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			return "\r\n" + sw.toString() + "\r\n";
		} catch (Exception e) {
			return "bad getErrorInfoFromException";
		}
	}
	
}
