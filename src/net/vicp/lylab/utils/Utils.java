package net.vicp.lylab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.utils.convert.JsonConverUtil;
import net.vicp.lylab.utils.convert.XmlConverUtil;

public abstract class Utils extends NonCloneableBaseObject {
	/**
	 * Close an object, safe if item is null
	 * @param target to be closed item
	 */
	public static void tryClose(Object target) {
		if (target instanceof AutoCloseable)
			try {
				((AutoCloseable) target).close();
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
	}

	/**
	 * Set value(param) to owner's field(based on fieldName)
	 * @param owner
	 * @param fieldName
	 * @param param
	 * @return
	 */
	public static void setter(Object owner, String fieldName, Object param) {
		if (fieldName == null)
			throw new NullPointerException("Parameter fieldName is null");
		Method setMethod = getSetterForField(owner, fieldName);
		if (setMethod == null)
			throw new LYException("No available setter was found for field[" + fieldName + "] " + owner.getClass());
		setter(owner, setMethod, param);
	}

	/**
	 * Set value(param) to owner's field(based on fieldName)
	 * @param owner
	 * @param fieldName
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void setter(Object owner, Method setMethod, Object param) {
		if (param == null)
			throw new NullPointerException("Parameter is null for setter[" + setMethod.getName() + "]");
		String setter = setMethod.getName();
		Class<?> parameterClass = setMethod.getParameterTypes()[0];
		try {
			// Should I throw exception?
			if (!setMethod.isAccessible())
				setMethod.setAccessible(true);
			// If we got its setter, then invoke it
			if (Caster.isGenericArrayType(param))
				setMethod.invoke(owner, Caster.arrayCast((List<Object>) param, parameterClass));
			else if (param.getClass() == String.class)
				setMethod.invoke(owner, Caster.simpleCast((String) param, parameterClass));
			else if (Caster.isBasicType(param))
				setMethod.invoke(owner, Caster.simpleCast(param.toString(), parameterClass));
			else
				setMethod.invoke(owner, param);
		} catch (Exception e) {
			throw new LYException("Cannot invoke setter[" + setter + "] for " + owner.getClass(), e);
		}
	}

	public static Method getSetterForField(Object owner, String fieldName) {
		if (fieldName == null)
			throw new NullPointerException("Parameter fieldName is null");
		String setter = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		if (owner == null)
			throw new NullPointerException("Owner is null for setter[" + setter + "]");
		// Get setter by java.lang.reflect.*
		Method setMethod = null;
		Set<Method> methods = new HashSet<>();
		methods.addAll(Arrays.asList(owner.getClass().getDeclaredMethods()));
		methods.addAll(Arrays.asList(owner.getClass().getMethods()));
		for (Method method : methods) {
			if (method.getName().equals(setter)) {
				Class<?>[] pts = method.getParameterTypes();
				if (pts.length != 1)
					continue;
				setMethod = method;
				break;
			}
		}

		return setMethod;
	}

	/**
	 * Get value from owner's field(based on fieldName)
	 * @param owner
	 * @param fieldName
	 * @return
	 */
	public static Object getter(Object owner, String fieldName) {
		if (owner == null)
			throw new NullPointerException("Owner is null for getter[" + fieldName + "]");
		String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		String isGetter = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		// Get getter by java.lang.reflect.*
		try {
			Method getMethod = null;
			try {
				getMethod = owner.getClass().getDeclaredMethod(getter);
			} catch (NoSuchMethodException e) { }
			try {
				getMethod = owner.getClass().getMethod(getter);
			} catch (NoSuchMethodException e) { }
			try {
				getMethod = owner.getClass().getDeclaredMethod(isGetter);
			} catch (NoSuchMethodException e) { }
			try {
				getMethod = owner.getClass().getMethod(isGetter);
			} catch (NoSuchMethodException e) { }
			if(getMethod == null)
				throw new LYException("No getter[" + getter + "] was found in " + owner.getClass() + "");
			// Should I throw exception?
			if(!getMethod.isAccessible())
				getMethod.setAccessible(true);
			return getMethod.invoke(owner);
		} catch (Exception e) {
			throw new LYException("Invoke getter[" + getter + "] for " + owner.getClass() + " failed", e);
		}
	}
	
	/**
	 * delete CRLF; delete empty line ;delete blank lines
	 * 
	 * @param input
	 * @return
	 */
	public static String deleteCRLF(String input) {
		return input.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
	}
	
	/**
	 * delete ALL invisible chars
	 * 
	 * @param input
	 * @return
	 */
	public static String deleteInvisible(String input) {
		return input.replaceAll("([\\s\b\\u0000-\\u0020]*)", "");
	}

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
	 * 
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
	 * 
	 * @param value
	 * @param pattern
	 * @return
	 */
	public static String format(Double value, String pattern) {
		return new DecimalFormat(pattern).format(value);
	}

	/**
	 * 格式化时间
	 * 
	 * @param value
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		return DateFormatUtils.format(date, pattern);
	}

	/**
	 * 创建目录
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean createDirectoryByFileName(String fileName) {
		try {
			if (existsFolder(fileName))
				return true;
			int max = fileName.lastIndexOf("\\");
			int b = fileName.lastIndexOf("/");
			max = (max > b ? max : b);
			File file = new File(fileName.substring(0, max));
			return file.mkdirs();
		} catch (Exception e) {
			return false;
		}
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
	public static Object[] readJsonObjectFromFile(Class<?> instanceClass, String filePath, String fileSuffix) {
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
					if (filePath.endsWith("/") || filePath.endsWith("\\"))
						filename = filePath + filelist[i];
					else
						filename = filePath + "\\" + filelist[i];
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
		if (!file.exists())
			throw new LYException("File not found:" + fileName);
		if (!file.isFile())
			throw new LYException("Given path is not a file:" + fileName);
		try (FileInputStream in = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, CoreDef.CHARSET()));) {
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

	public static <T> boolean inList(T[] list, Object item) {
		if (list == null || item == null || list.length == 0)
			return false;
		for (T o : list)
			if (item.equals(o))
				return true;
		return false;
	}

	public static boolean inList(@SuppressWarnings("rawtypes") List list, Object item) {
		if (list == null || item == null || list.isEmpty() || !list.contains(item))
			return false;
		return true;
	}

	/**
	 * 获取异常的字符串内容
	 * @param e Exception itself
	 * @return
	 */
	public static String getStringFromException(Exception e) {
		return getStringFromThrowable(e);
	}

	/**
	 * 获取错误的字符串内容
	 * @param e Error itself
	 * @return
	 */
	public static String getStringFromError(Error e) {
		return getStringFromThrowable(e);
	}

	/**
	 * 获取异常/错误的字符串内容
	 * @param t Any throwable object
	 * @return
	 */
	public static String getStringFromThrowable(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return "\r\n" + sw.toString() + "\r\n";
	}

	/**
	 * XML to Object, <b>ONLY</b> support simple format like follow:
	 * <br>&lt;key&gt;value&lt;/key&gt;
	 * 
	 * @param xml
	 * @return
	 * 
	 */
	public static Map<String, Object> xml2Object(String xml) {
		return XmlConverUtil.xml2Object(xml);
	}

	public static String serialize(Object obj) {
		return JsonConverUtil.serialize(obj);
	}

	public static <T> T deserialize(Class<T> instanceClass, String json) {
		return JsonConverUtil.deserialize(instanceClass, json);
	}

	public static Object deserialize(String className, String json) {
		return JsonConverUtil.deserialize(className, json);
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
		if (CoreDef.BIG_ENDIAN) {
			bytes[3] = int3(x);
			bytes[2] = int2(x);
			bytes[1] = int1(x);
			bytes[0] = int0(x);
		} else {
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
			throw new LYException("Out of bounds, byte length is " + bytes.length + " while offset is " + offset);

		if (CoreDef.BIG_ENDIAN)
			return makeInt(bytes[offset + 3], bytes[offset + 2], bytes[offset + 1], bytes[offset + 0]);
		else
			return makeInt(bytes[offset + 0], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]);
	}

	/**
	 * Copy bytes from List into byte[]
	 * @param container
	 * @return
	 */
	public static byte[] copyBytesFromContainer(List<Byte> container) {
		if (container == null)
			throw new LYException("Parameter container is null");
		byte[] bytes = new byte[container.size()];
		for (int i = 0; i < container.size(); i++)
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

	public static boolean bytecat_isCapable(byte[] dst, int dstOffset, int srcCopyLength) {
		if (dst.length - dstOffset < srcCopyLength)
			return false;
		return true;
	}

	public static byte[] bytecat(byte[] dst, int dstOffset, byte[] src, int srcOffset, int srcCopyLength) {
		if (!bytecat_isCapable(dst, dstOffset, srcCopyLength))
			dst = Arrays.copyOf(dst, dst.length*CoreDef.SOCKET_MAX_BUFFER_EXTEND_RATE);
//			throw new IndexOutOfBoundsException("Destination byte[] hasn't enough ");
		for (int i = 0; i < srcCopyLength; i++)
			dst[dstOffset + i] = src[srcOffset + i];
		return dst;
	}
	
	/**
	 * Compare bytes start from a specific position for a specific limit
	 * 
	 * @param e1
	 *            source to compare
	 * @param e1Offset
	 *            offset from e1
	 * @param e2
	 *            destination to compare
	 * @param e2Offset
	 *            offset from e2
	 * @param cmpLength
	 *            compare for specific length
	 * @return true if e1 and e2 is the same from e1Offset and e2Offset for
	 *         comLength
	 */
	public static boolean bytesContinueWith(byte[] e1, int e1Offset, byte[] e2,
			int e2Offset, int cmpLength) {
		if (e1.length - e1Offset < e2.length - e2Offset
				|| e1.length - e1Offset < cmpLength
				|| e2.length - e2Offset < cmpLength)
			return false;
		for (int i = 0; i < cmpLength; i++)
			if (e1[e1Offset + i] != e2[e2Offset + i])
				return false;
		return true;
	}

	public static boolean checkHead(byte[] bytes, int offset, byte[] head) {
		return checkHead(bytes, offset, bytes.length - offset, head);
	}

	public static boolean checkHead(byte[] bytes, int offset, int length, byte[] head) {
		if (length < head.length)
			return true;
		return bytesContinueWith(bytes, offset, head, 0, head.length);
	}

}
