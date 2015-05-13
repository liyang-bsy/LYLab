package net.vicp.lylab.utils;

import java.io.File;

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
}
