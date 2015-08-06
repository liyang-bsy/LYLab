package net.vicp.lylab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.AutoInitialize;
import net.vicp.lylab.utils.atomic.AtomicStrongReference;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils extends NonCloneableBaseObject {

	/**
	 * 路径为文件且不为空则进行删除
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 目录是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean existsFolder(String path) {
		try {
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 识别字符串是否全是中文编码 or ASCII code
	 * 
	 * @param strName
	 * @return
	 */
	public static boolean isAllChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++)
			if (!isChinese(ch[i]) && !isAscii(ch[i]))
				return false;
		return true;
	}

	/**
	 * Test a char is ASCII code
	 * @param c
	 * @return
	 */
	private static boolean isAscii(char c) {
		if (c < 128 && c > 0)
			return true;
		return false;
	}
	/**
	 * 识别字符是否是中文编码
	 * 
	 * @param c
	 * @return
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 格式化数字
	 * @param value
	 * @param pattern
	 * @return
	 */
	public static String format(Double value, String pattern)
	{
		if(value == null) return null;
		return new DecimalFormat(pattern).format(value);
	}

	/**
	 * 格式化时间
	 * @param value
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern)
	{
		try {
			if(date == null) return null;
			return DateFormatUtils.format(date,pattern);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createDirectory(String path) {
		try {
			if (existsFolder(path))
				return true;
			File file = new File(path);
			return file.mkdirs();
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 读取目录下特定后缀的文件，并且以instanceClass生成该对象
	 * @param instanceClass
	 * @param filePath
	 * @param fileSuffix
	 * @return
	 */
	public static Object[] readJsonObjectFromFile(Class<?> instanceClass,
			String filePath, String fileSuffix) {
		List<String> fileNames = getFileList(filePath, fileSuffix);
		List<Object> list = new ArrayList<Object>();
		for (String fileName : fileNames) {
			for (String json : Utils.readFileByLines(fileName))
				list.add(deserialize(instanceClass, json));
			deleteFile(fileName);
		}
		Object[] objects = new Object[list.size()];
		list.toArray(objects);
		return objects;
	}
	
	/**
	 * 根据路径和文件名后缀，获取文件列表的绝对路径
	 * @param filePath
	 * @param fileSuffix e.g. ".txt"
	 * @return
	 */
	public static List<String> getFileList(String filePath, String fileSuffix) {
		List<String> ret = new ArrayList<String>();
		try {
			File file = new File(filePath);
			if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					String filename;
					// 避免双斜杠目录
					if(filePath.endsWith("/") || filePath.endsWith("\\"))
						filename = filePath + filelist[i];
					else filename = filePath + "\\" + filelist[i];
					// 如果以该后缀结尾，假如不是，否则忽略该文件
					if (filename.endsWith(fileSuffix))
						ret.add(filename);
				}
			}
		} catch (Exception e) { }
		return ret;
	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static List<String> readFileByLines(String fileName)
	{
		return readFileByLines(fileName, true);
	}
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static List<String> readFileByLines(String fileName, boolean ignoreEmptyLine) {
		List<String> ret = new ArrayList<String>();
		File file = new File(fileName);
		if(!file.exists())
			throw new LYException("File not found:" + fileName);
		if(!file.isFile())
			throw new LYException("Given path is not a file:" + fileName);
		try (FileInputStream in = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, CoreDef.CHARSET()));) {
			String tempString = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 忽略空行
				if (ignoreEmptyLine && tempString.equals(""))
					continue;
				// 移除BOM头
				byte[] b = tempString.getBytes();
				if (b.length > 3 && b[0] == -17 && b[1] == -69 && b[2] == -65)
					tempString = tempString.substring(1);
				// 忽略空行
				if (ignoreEmptyLine && tempString.trim().equals(""))
					continue;
				ret.add(tempString.trim());
			}
			reader.close();
		} catch (Exception e) {
			throw new LYException("Read file failed", e);
		}
		return ret;
	}

	public static boolean inList(Object[] list, Object item) {
		if(list == null || item == null) return false;
		for(Object o:list)
			if(o.equals(item))
				return true;
		return false;
	}

	public static boolean inList(@SuppressWarnings("rawtypes") List list, Object item) {
		if (list == null || item == null || list.isEmpty()
				|| !list.contains(item))
			return false;
		return true;
	}

	/**
	 * 获取异常的字符串内容
	 * @param t
	 * @return
	 */
	public static String getStringFromException(Exception e) {
		return getStringFromThrowable(e);
	}


	/**
	 * 获取错误的字符串内容
	 * @param t
	 * @return
	 */
	public static String getStringFromError(Error e) {
		return getStringFromThrowable(e);
	}


	/**
	 * 获取异常/错误的字符串内容
	 * @param t
	 * @return
	 */
	public static String getStringFromThrowable(Throwable t) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			return "\r\n" + sw.toString() + "\r\n";
		} catch (Exception ex) {
			return "bad getErrorInfoFromException";
		}
	}

	private static AutoInitialize<JsonFactory> jsonFactory = new AtomicStrongReference<JsonFactory>();
	private static AutoInitialize<ObjectMapper> mapper = new AtomicStrongReference<ObjectMapper>();

	public static String serialize(Object obj)
	{
		if(obj == null)
			throw new LYException("Parameter obj is null");
		String str = null;
		try {
			StringWriter sw = new StringWriter();
			JsonGenerator generator = jsonFactory.get(JsonFactory.class).createGenerator(sw);
			mapper.get(ObjectMapper.class).writeValue(generator, obj);
			str = sw.toString();
			generator.close();
		} catch (Exception e) { throw new LYException("Serialize failed", e); }
	    
		return str;
	}
	
	public static Object deserialize(Class<?> instanceClass, String json)
	{
		if(json == null)
			throw new LYException("Parameter json is null");
		if(instanceClass == null)
			throw new LYException("Parameter instanceClass is null");
		try {
			return mapper.get(ObjectMapper.class).readValue(json, instanceClass);  
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json[" + json + "] into " + instanceClass.getName(), e);
		}
	}
	
	public static Object deserialize(String className, String json)
	{
		if(json == null)
			throw new LYException("Parameter json is null");
		if(className == null)
			throw new LYException("Parameter className is null");
		try {
			return mapper.get(ObjectMapper.class).readValue(json, Class.forName(className));  
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json into " + className + "\n" + json, e);
		}
	}

	private static byte int3(int x) { return (byte) (x >> 24); }
	private static byte int2(int x) { return (byte) (x >> 16); }
	private static byte int1(int x) { return (byte) (x >> 8); }
	private static byte int0(int x) { return (byte) (x); }

	private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}
    
	/**
	 * 数字转byte
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] IntToBytes4(int x) {
		byte[] bytes = new byte[4];
		if(CoreDef.BIG_ENDIAN) {
			bytes[3] = int3(x);
			bytes[2] = int2(x);
			bytes[1] = int1(x);
			bytes[0] = int0(x);
		}
		else {
			bytes[0] = int3(x);
			bytes[1] = int2(x);
			bytes[2] = int1(x);
			bytes[3] = int0(x);
		}
		return bytes;
	}

	/**
	 * byte转数字
	 * 
	 * @param bytes
	 * @return
	 */
	public static int Bytes4ToInt(byte[] bytes) {
		if (CoreDef.BIG_ENDIAN)
			return makeInt(bytes[3], bytes[2], bytes[1], bytes[0]);
		else
			return makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	/**
	 * byte转数字
	 * 
	 * @param bytes
	 * @return
	 */
	public static int Bytes4ToInt(byte[] bytes, int offset) {
		if (bytes.length - 4 < offset)
			throw new LYException("Out of bounds, byte length is " + bytes.length
					+ " while offset is " + offset);

		if (CoreDef.BIG_ENDIAN)
			return makeInt(bytes[offset + 3], bytes[offset + 2],
					bytes[offset + 1], bytes[offset + 0]);
		else
			return makeInt(bytes[offset + 0], bytes[offset + 1],
					bytes[offset + 2], bytes[offset + 3]);
	}
	
	/**
	 * Copy bytes from List into byte[]
	 * @param container
	 * @return
	 */
	public static byte[] copyBytesFromContainer(List<Byte> container)
	{
		if(container == null) throw new LYException("Parameter container is null");
		byte[] bytes = new byte[container.size()];
		for(int i=0;i<container.size();i++)
			bytes[i] = container.get(i);
		return bytes;
	}
	
	/**
	 * Move bytes from array into List(original array will be reset)
	 * @param container
	 * @return
	 */
	public static List<Byte> moveBytesToContainer(byte[] bytes) {
		List<Byte> container = new ArrayList<Byte>();
		for (int i = 0; i < bytes.length; i++) {
			container.add(bytes[i]);
			bytes[i] = 0;
		}
		return container;
	}
	
}
