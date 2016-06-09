package net.vicp.lylab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.time.DateFormatUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.utils.convert.JsonConverUtil;
import net.vicp.lylab.utils.convert.XmlConverUtil;

public abstract class Utils extends NonCloneableBaseObject {

	/**
	 * Print current stack
	 * @param only accept "debug", "info", "warn", "error", "fatal", case-insensitive
	 */
	public final static void printStack(String level) {
		printStack("", level);
	}
	
	/**
	 * Print current stack with simple reasons
	 * @param reason
	 * @param only accept "debug", "info", "warn", "error", "fatal", case-insensitive
	 */
	public final static void printStack(String reason, String level) {
		if (level == null)
			level = "debug";
		if (reason == null)
			throw new NullPointerException("Parameter reason is null");
		switch (level.toLowerCase()) {
		case "debug":
			log.debug(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Stack is printed below")));
			break;
		case "info":
			log.info(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Stack is printed below")));
			break;
		case "warn":
			log.warn(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Stack is printed below")));
			break;
		case "error":
			log.error(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Stack is printed below")));
			break;
		case "fatal":
			log.fatal(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Stack is printed below")));
			break;
		default:
			log.debug(reason + CoreDef.LINE_SEPARATOR + getStringFromException(new LYException("Print stack with bad param:[" + level + "]")));
			break;
		}
	}
	
	/**
	 * Close an object, safe if item is null
	 * @param target to be closed item, it's safe if targets were null
	 */
	public final static void tryClose(Object... targets) {
		printStack("TryClose is called on follow target(s):\n" + Arrays.deepToString(targets), "debug");
		if (targets == null)
			return;
		for (Object target : targets)
			if (target instanceof AutoCloseable)
				try {
					((AutoCloseable) target).close();
				} catch (Throwable t) {
					log.error("Close target failed:" + Utils.getStringFromThrowable(t));
				}
	}

	public final static InetAddr getPeer(SocketChannel socketChannel) {
		Socket socket = socketChannel.socket();
		return getPeer(socket);
	}

	public final static InetAddr getPeer(Socket socket) {
		return InetAddr.fromInetAddr(socket.getInetAddress().getHostAddress(), socket.getPort());
	}

	public final static Object convertAndInvoke(Object owner, Method method, Object... parameters)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] parameterClasses = method.getParameterTypes();
		if (parameterClasses.length != parameters.length)
			throw new LYException("Dismatch parameters with method[" + method.getName() + "]:" + Arrays.deepToString(parameters));
		Object[] convertedParams = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Object temp = parameters[i];
			if (!temp.getClass().equals(parameterClasses[i])) {
				if (Caster.isBasicType(parameterClasses[i]) && Caster.isBasicType(temp))
					temp = Caster.simpleCast(parameters[i].toString(), parameterClasses[i]);
				else if (Caster.isGenericArrayType(parameterClasses[i]) || Caster.isGenericArrayType(temp)) {
					if (temp instanceof Collection)
						temp = Caster.arrayCast((Collection<?>) parameters[i], parameterClasses[i]);
					if (temp instanceof Collection)
						temp = Caster.arrayCast(Arrays.asList(parameters[i]), parameterClasses[i]);
				} else
					throw new LYException("Method[" + method.getName() + "] requires parameter "
							+ parameterClasses[i].getName() + ", but provided parameter is "
							+ temp.getClass().getName());
			}
			convertedParams[i] = temp;
		}
		return method.invoke(owner, parameters);
	}

	/**
	 * Set value(param) to owner's field(based on fieldName)
	 * @param owner
	 * @param fieldName
	 * @param param
	 * @return
	 */
	public final static void setter(Object owner, String fieldName, Object param) {
		if (fieldName == null)
			throw new NullPointerException("Parameter fieldName is null");
		Method setMethod = getSetterForField(owner, fieldName, param);
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
	public final static void setter(Object owner, Method setMethod, Object param) {
		if (param == null)
			throw new NullPointerException("Parameter is null for setter[" + setMethod.getName() + "]");
		String setter = setMethod.getName();
		Class<?> parameterClass = setMethod.getParameterTypes()[0];
		Class<?> originalClass = param.getClass();
		try {
			// Should I throw exception?
			if (!setMethod.isAccessible())
				setMethod.setAccessible(true);
			// If we got its setter, then invoke it
			if(parameterClass.isAssignableFrom(originalClass))
				setMethod.invoke(owner, param);
			else if (Caster.isGenericArrayType(originalClass) && Caster.isGenericArrayType(parameterClass))
				setMethod.invoke(owner, Caster.arrayCast((List<Object>) param, parameterClass));
			else if (Caster.isBasicType(originalClass) && Caster.isBasicType(parameterClass))
				setMethod.invoke(owner, Caster.simpleCast(param, parameterClass));
			else
				throw new LYException("Cannot convert param from [" + param.getClass() + "] to [" + parameterClass + "]");
		} catch (Exception e) {
			throw new LYException("Cannot invoke setter[" + setter + "] for " + owner.getClass(), e);
		}
	}

	public final static Method getSetterForField(Object owner, String fieldName, Object param) {
		if (fieldName == null)
			throw new NullPointerException("Parameter fieldName is null");
		String setter = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		if (owner == null)
			throw new NullPointerException("Owner is null for setter[" + setter + "]");
		// Get setter by java.lang.reflect.*
		Method setMethod = null;
		try {
			setMethod = owner.getClass().getDeclaredMethod(setter, param.getClass());
		} catch (Exception e) { }
		if (setMethod != null)
			return setMethod;
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
	public final static Object getter(Object owner, String fieldName) {
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
	public final static String deleteCRLF(String input) {
		return input.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
	}
	
	/**
	 * delete ALL invisible chars
	 * 
	 * @param input
	 * @return
	 */
	public final static String deleteInvisible(String input) {
		return input.replaceAll("([\\s\b\\u0000-\\u0020]*)", "");
	}

	/**
	 * 路径为文件且不为空则进行删除
	 * 
	 * @param path
	 * @return
	 */
	public final static boolean deleteFile(String path) {
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
	public final static boolean existsFolder(String path) {
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
	public final static boolean isAllChinese(String strName) {
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
	private final static boolean isAscii(char c) {
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
	private final static boolean isChinese(char c) {
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
	public final static String format(Double value, String pattern) {
		return new DecimalFormat(pattern).format(value);
	}

	/**
	 * 格式化时间
	 * 
	 * @param value
	 * @param pattern
	 * @return
	 */
	public final static String format(Date date, String pattern) {
		return DateFormatUtils.format(date, pattern);
	}

	/**
	 * 创建目录
	 * 
	 * @param fileName
	 * @return
	 */
	public final static boolean createDirectoryByFileName(String fileName) {
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
	public final static boolean createDirectory(String path) {
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
	public final static Object[] readJsonObjectFromFile(Class<?> instanceClass, String filePath, String fileSuffix) {
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
	 * 根据系统类型格式化文件路径
	 * @param filePath
	 * @return
	 */
	public final static String formatFilePath(String filePath) {
		return filePath.replaceAll("/", "\\" + File.separator).replaceAll("\\\\", "\\" + File.separator);
	}

	/**
	 * 根据路径和文件名后缀，获取文件列表的绝对路径
	 * @param filePath
	 * @param fileSuffix e.g. ".txt", null for All files(exclude directory)
	 * @return
	 */
	public final static List<String> getFileList(String filePath, String fileSuffix) {
		List<String> ret = new ArrayList<String>();
		filePath = formatFilePath(filePath);
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
						filename = filePath + File.separator + filelist[i];
					// 如果以该后缀结尾，假如不是，否则忽略该文件
					if (fileSuffix == null || filename.endsWith(fileSuffix))
						ret.add(filename);
				}
			}
		} catch (Exception e) { }
		return ret;
	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public final static List<String> readFileByLines(String fileName) {
		return readFileByLines(fileName, true, true);
	}
	
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 * @param fileName 文件名
	 * @param ignoreEmptyLine true则将空行略过
	 * @param trimResult true则将结果字符串执行trim()操作
	 * @return
	 */
	public final static List<String> readFileByLines(String fileName, boolean ignoreEmptyLine, boolean trimResult) {
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
				if(trimResult) tempString = tempString.trim();
				if (ignoreEmptyLine && tempString.equals(""))
					continue;
				ret.add(tempString);
			}
			reader.close();
		} catch (Exception e) {
			throw new LYException("Read file failed", e);
		}
		return ret;
	}

	public final static <T> boolean inList(T[] list, Object item) {
		if (list == null || item == null || list.length == 0)
			return false;
		for (T o : list)
			if (item.equals(o))
				return true;
		return false;
	}

	public final static boolean inList(@SuppressWarnings("rawtypes") List list, Object item) {
		if (list == null || item == null || list.isEmpty() || !list.contains(item))
			return false;
		return true;
	}

	/**
	 * 获取异常的字符串内容
	 * @param e Exception itself
	 * @return
	 */
	public final static String getStringFromException(Exception e) {
		return getStringFromThrowable(e);
	}

	/**
	 * 获取错误的字符串内容
	 * @param e Error itself
	 * @return
	 */
	public final static String getStringFromError(Error e) {
		return getStringFromThrowable(e);
	}

	/**
	 * 获取异常/错误的字符串内容
	 * @param t Any throwable object
	 * @return
	 */
	public final static String getStringFromThrowable(Throwable t) {
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
	public final static Map<String, Object> xml2Object(String xml) {
		return XmlConverUtil.xml2Object(xml);
	}

	public final static String serialize(Object obj) {
		return JsonConverUtil.serialize(obj);
	}

	public final static <T> T deserialize(Class<T> instanceClass, String json) {
		return JsonConverUtil.deserialize(instanceClass, json);
	}

	public final static Object deserialize(String className, String json) {
		return JsonConverUtil.deserialize(className, json);
	}

	private final static byte int3(int x) { return (byte) (x >> 24); }
	private final static byte int2(int x) { return (byte) (x >> 16); }
	private final static byte int1(int x) { return (byte) (x >> 8); }
	private final static byte int0(int x) { return (byte) (x); }

	private final static int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	/**
	 * Boolean to byte
	 * 
	 * @param num
	 * @return
	 */
	public final static byte[] boolean2Bytes(boolean b) {
		return new byte[] { (byte) (b ? 1 : 0) };
	}

	/**
	 * Byte to boolean
	 * 
	 * @param bytes
	 * @return
	 */
	public final static boolean bytes2Boolean(byte[] bytes, int offset) {
		if (bytes.length - 1 < offset)
			throw new LYException("Out of bounds, byte length is " + bytes.length + " while offset is " + offset);

		if (bytes[0] == 0)
			return false;
		return true;
	}

	public final static boolean bytes2Boolean(byte[] bytes) {
		return bytes2Boolean(bytes,0);
	}

	/**
	 * Integer to byte
	 * 
	 * @param num
	 * @return
	 */
	public final static byte[] int2Bytes(int x) {
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
	 * Byte to integer
	 * 
	 * @param bytes
	 * @return
	 */
	public final static int bytes2Int(byte[] bytes) {
		if (CoreDef.BIG_ENDIAN)
			return makeInt(bytes[3], bytes[2], bytes[1], bytes[0]);
		else
			return makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	/**
	 * Byte to integer
	 * 
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public final static int bytes2Int(byte[] bytes, int offset) {
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
	public final static byte[] copyBytesFromContainer(List<Byte> container) {
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
	public final static List<Byte> moveBytesToContainer(byte[] bytes) {
		List<Byte> container = new ArrayList<Byte>();
		for (int i = 0; i < bytes.length; i++) {
			container.add(bytes[i]);
			bytes[i] = 0;
		}
		return container;
	}

	public final static boolean bytecat_isCapable(byte[] dst, int dstOffset, int srcCopyLength) {
		if (dst.length - dstOffset < srcCopyLength)
			return false;
		return true;
	}

	public final static byte[] bytecat(byte[] dst, int dstOffset, byte[] src, int srcOffset, int srcCopyLength) {
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
	public final static boolean bytesContinueWith(byte[] e1, int e1Offset, byte[] e2,
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

	public final static boolean checkHead(byte[] bytes, int offset, byte[] head) {
		return checkHead(bytes, offset, bytes.length - offset, head);
	}

	public final static boolean checkHead(byte[] bytes, int offset, int length, byte[] head) {
		if (length < head.length)
			return true;
		return bytesContinueWith(bytes, offset, head, 0, head.length);
	}

	public final static int writeNext(byte[] bytes, int offset, byte[] next) {
		System.arraycopy(next, 0, bytes, offset, next.length);
		return offset + next.length;
	}

	private static Random random = new Random();

	public static String createRandomNumberCode(int length) {
		if (length >= 10)
			throw new InvalidParameterException("Only support Integer range");
		int max = 1;
		for (int i = 0; i < length; i++)
			max *= 10;
		StringBuilder result = new StringBuilder();
		result.append(random.nextInt(max));
		while (result.toString().length() < length)
			result.insert(0, "0");
		return result.toString();
	}

	public final static String createUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public final static String sqlOrderByAntiInjection(String order) {
		return order.replaceAll("[^A-Za-z0-9_,]", "");
	}
	
	/** 
	 * 验证输入的邮箱格式是否符合 
	 * @param email 
	 * @return 是否合法 
	 */ 
	public final static boolean isEmailAddress(String email) {
		return email.matches("^([a-z0-9A-Z]+[-|//.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?//.)+[a-zA-Z]{2,}$");
	}
	
	/** 
	 * 验证输入的邮箱格式是否符合 
	 * @param email 
	 * @return 是否合法 
	 */ 
	public final static boolean isNumeric(String number) {
		return number.matches("[0-9]*");
	}
	
	public static boolean isMobileNumber(String mobile) {
		return mobile.matches("^[1][3,4,5,8][0-9]{9}$");
	}
	
	/**
	 * 电话号码验证
	 * @param phoneNo
	 * @return 验证通过返回true
	 */
	public static boolean isPhone(String phoneNo) {
		if (phoneNo.length() > 9) {
			return phoneNo.matches("^[0][1-9]{2,3}-[0-9]{5,10}$"); // 验证带区号的
		} else {
			return phoneNo.matches("^[1-9]{1}[0-9]{5,8}$"); // 验证没有区号的
		}
	}

}
