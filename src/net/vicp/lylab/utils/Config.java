package net.vicp.lylab.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.Pair;

/**
 * Powerful config object for global uses, support specific program mark
 * <br>
 * <br>Follow marks were supported
 * <br><br><b>Mode mark(first line only):</b> {@code [TREE]} or {@code [PLAIN]}, {@code [TREE]} is default
 * <br><br><b>Comment mark:</b> key(start with #) or bad key/value will be regard as comment
 * <br>Example:#comment = o(*≧▽≦)ツ
 * <br>Well, it will be nothing...
 * <br><br><b>Object mark:</b> the value of key(start with *) should be class name, stored its new instance. If value(start with &), will be regard as key and try to find any the key from itself or its parents.
 * <br>Example:*object1=com.java.ExampleClass
 * <br>You can access "object1" by {@link #getObject(String key)}: {@code config.getObject("object1")};
 * <br><b>[!]</b>ExampleClass <b>MUST</b> have a default constructor
 * <br><br><b>Parameter mark:</b> the value of key(start with ^) will be set to last Object(* mark). If value(start with &), will be regard as key and try to find any the key from itself or its parents.
 * <br>Example:^value1=123.45
 * <br>{@code exampleClass.setValue1(123.45);}
 * <br>Its key will finally be dropped, certainly never replace any existed keys
 * <br><br><b>Configuration mark:</b> key(start with $) will be regard as another {@link Config}
 * <br>Example:$config1=dir/next_config.txt
 * <br>You can access "object1" by {@link #getObject(String key)} or {@link #getConfig(String key)} : {@code config.getConfig("config1")};
 * <br>If sub-config is {@code [PLAIN]}, all its entry will be obtain into current config
 * <br>
 * <br>Different between Tree/Plain {@link Config}
 * <br>Tree-Configuration {@link Config} could get it sub-config by {@link #getConfig(String key)}
 * <br>Plain-Configuration will obtain all sub-config into itself
 * <br>
 * <br>Entry rule:
 * <br>Entry start with '#' will be regard as comment and ignored for dataMap;
 * <br>Key may start with function mark '$'/'*'/'^', itself contains underline, number or alphabet
 * <br>value may start with function mark '&', itself may contain any visible character except '&'
 * 
 * @author Young
 * @since 2015.07.29
 * @version 2.1.0
 *
 */
public final class Config extends NonCloneableBaseObject {
	// Regular expression, start with any thing except '[', and end with '[number]'
	// May used for future array/switch-value support
	// Pattern.compile("^[^\\[]+[\\[][0-9]*[\\]]$").matcher("65435632[554234]").find()

	public static void main(String[] arg) {
		System.out.println(new Config(CoreDef.rootPath + "\\config\\A.txt"));
	}

	private transient String fileName;
	private Map<String, Object> dataMap;
	private Map<String, Object> tmpMap;
	private List<String> keyList = new ArrayList<String>();
	private transient Config parent;
	private int mode = 0;
	private transient Stack<String> fileNameTrace;
	private transient List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
	private transient List<Pair<String, String>> lazyLoad = new ArrayList<Pair<String,String>>();

//	public static final String INVISIBLE_CHAR = "[\u0000-\u0020]";
	public static final String VALID_NAME = "([$*^][_\\w]*)|([_\\w]*)";
	public static final String VALID_VALUE = "([&][^&]*)|([^&]*)";
	
	public static final Map<Character, Integer> sortRule = new HashMap<Character, Integer>();
	public static final Set<Character> lazyLoadSet;
	static {
		sortRule.put('*', 50);
		sortRule.put('^', 50);
		sortRule.put('$', 100);
		lazyLoadSet = sortRule.keySet();
	}

	/**
	 *  Create an empty config
	 */
	public Config() {
		this(null, null, null);
	}
	
	/**
	 * Create a config with specific file name
	 * @param fileName
	 */
	public Config(String fileName) {
		this(fileName, new Stack<String>(), null);
	}

	private Config(String fileName, Stack<String> fileNameTrace, Config parent) {
		this.fileName = fileName;
		this.fileNameTrace = fileNameTrace;
		this.parent = parent;
		dataMap = null;
		reload();
	}

	/**
	 * You may reload your config manually
	 * @param fileName
	 * @param fileNameTrace
	 * @param parent
	 */
	public synchronized void reload() {
		if (fileName == null)
			return;
		// file trace tree
		fileNameTrace.push(fileName);
		// initial
		tmpMap = new ConcurrentHashMap<String, Object>();
		// clear old data
		lazyLoad.clear();
		properties.clear();
		// load key/value for loader
		rawLoader();
		for (int i = 0; i < properties.size(); i++) {
			Pair<String, String> property = properties.get(i);
			try {
				String propertyName = property.getLeft();
				// skip # and empty entry
				if (propertyName.equals("") || propertyName.startsWith("#"))
					continue;
				// mode define
				if (i == 0 && property.getRight() == null) {
					if (propertyName.equals("[TREE]"))
						mode = 0;
					if (propertyName.equals("[PLAIN]"))
						mode = 1;
					continue;
				}
				// lazy load or instant load
				if (isLazyLoad(propertyName))
					insertLazyLoad(property);
				else
					putToMap(tmpMap, propertyName, property.getRight());
			} catch (Exception e) {
				throw new LYException("Failed to load config file[" + fileName
						+ "] at line [" + getLine(property) + "]", e);
			}
		}
		if (!lazyLoad.isEmpty())
			lazyLoad();
		dataMap = tmpMap;
		tmpMap = null;
		fileNameTrace.pop();
	}

	/**
	 * Returns true if this property should do lazy load.
	 * @return
	 * true if this property should do lazy load
	 */
	private boolean isLazyLoad(String propertyName) {
		for (char singal : lazyLoadSet) {
			if (propertyName.charAt(0) == singal)
				return true;
		}
		return false;
	}
	
	// Simulate a safely insert-sort
	private void insertLazyLoad(Pair<String, String> property) {
		int i=0;
		for(;i<lazyLoad.size();i++)
			if(sortRule.get(property.getLeft().charAt(0)) < sortRule.get(lazyLoad.get(i).getLeft().charAt(0)))
				break;
		lazyLoad.add(i,property);
		log.debug(property);
		
	}

	private void lazyLoad() {
		Object lastObject = null;
		Pair<String, String> property = null;
		try {
			for(int i=0;i<lazyLoad.size();i++) {
				property = lazyLoad.get(i);
				log.debug(property);
				String propertyName = property.getLeft();
				String propertyValue = property.getRight();
				if (propertyName.startsWith("$")) {
					// sub-config reference
					String propertyRealName = propertyName.substring(1);
					String realFileName = propertyValue;
					Config config = null;
					testfilepath: {
						// absolute path
						File testExist = null;
						testExist = new File(realFileName);
						if (testExist.exists() && testExist.isFile())
							break testfilepath;
						// canonical path
						int index = fileName.lastIndexOf(File.separator);
						String filePath = fileName.substring(0, index + 1);
						testExist = new File(filePath + realFileName);
						if (testExist.exists() && testExist.isFile())
							realFileName = filePath + realFileName;
					}
					// if exist circle
					if (fileNameTrace.contains(realFileName))
						throw new LYException(
								"Circle reference was found in config file["
										+ realFileName + "]");
					switch (mode) {
					case 0:
						config = new Config(realFileName, fileNameTrace, this);
						putToMap(tmpMap, propertyRealName, config);
						break;
					case 1:
						config = new Config(realFileName, fileNameTrace, this);
						readFromConfig(tmpMap, config);
						break;
					default:
						throw new LYException("Unknow config mode: " + mode);
					}
				} else if (propertyName.startsWith("*")) {
					// object reference
					String propertyRealName = propertyName.substring(1);
					Object target = null;
					if (propertyValue.startsWith("&")) {
						String propertyRealValue = propertyValue.substring(1);
						Object obj = null;
						Config root = this;
						while (true) {
							obj = root.tmpMap.get(propertyRealValue);
							if (obj != null || root.parent == null)
								break;
							root = parent;
						}
						if (obj == null)
							throw new LYException("Cannot find reference ["
									+ propertyRealValue
									+ "] in this Config or parent Config");
						target = obj;
					} else {
						target = Class.forName(propertyValue).newInstance();
						putToMap(tmpMap, propertyRealName, target);
					}
					lastObject = target;
				} else if (propertyName.startsWith("^")) {
					// object parameter reference
					String propertyRealName = propertyName.substring(1);
					if (propertyValue.startsWith("&")) {
						String propertyRealValue = propertyValue.substring(1);
						Object obj = null;
						Config root = this;
						while (true) {
							obj = root.tmpMap.get(propertyRealValue);
							if (obj != null || root.parent == null)
								break;
							root = parent;
						}
						if (obj == null)
							throw new LYException("Cannot find reference ["
									+ propertyRealValue
									+ "] in this Config or parent Config");
						setter(lastObject, propertyRealName, obj);
					} else
						setter(lastObject, propertyRealName, propertyValue);
				}
			}
		} catch (Exception e) {
			throw new LYException("Failed on lazy load config file[" + fileName
					+ "] at line [" + getLine(property) + "]", e);
		}
	}
	
	private int getLine(Pair<String, String> property) {
		for (int i = 0; i < properties.size(); i++)
			if (property.equals(properties.get(i)))
				return i;
		return -1;
	}
	
	private void setter(Object owner, String fieldName, Object param)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		if (owner == null)
			throw new NullPointerException("Owner is null for setter["
					+ fieldName + "]");
		if (param == null)
			throw new NullPointerException("Parameter is null for setter["
					+ fieldName + "]");
		String setter = "set" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
		
		// Get setter by java.lang.reflect.*
		Method setMethod = null;
		Class<?> parameterClass = null;
		try {
			setMethod = owner.getClass().getDeclaredMethod(setter, String.class);
			parameterClass = String.class;
		} catch (Exception e) { }
		if(setMethod == null)
			for (Method method : owner.getClass().getDeclaredMethods()) {
				if (method.getName().equals(setter)) {
					Class<?>[] pts = method.getParameterTypes();
					if (pts.length != 1)
						continue;
					parameterClass = pts[0];
					setMethod = method;
					break;
				}
			}
		// If we got its setter, then invoke it
		if (setMethod != null)
			if (param.getClass() == String.class)
				// But sometimes we may need casting parameter before invoking
				setMethod.invoke(owner,
						Caster.simpleCast((String) param, parameterClass));
			else
				setMethod.invoke(owner, param);
		else
			throw new LYException("No available setter[" + setter + "] was found for " + owner.getClass());
	}

	private void rawLoader() {
		List<String> rawList = Utils.readFileByLines(fileName, false);
		for (int i = 0; i < rawList.size(); i++) {
			// Trim
			String rawPair = rawList.get(i).replaceAll("([\\s]*)", "");
			// Remove comment
			rawPair = rawPair.replaceAll("([#][\\S]*)", "");
			
			String[] pair = rawPair.split("=");
			if (pair.length == 1) {
				if (i == 0 && pair[0].startsWith("[") && pair[0].endsWith("]"))
					properties.add(new Pair<String, String>(pair[0], null));
				else if(StringUtils.isBlank(pair[0]))
					properties.add(new Pair<String, String>("", null));
				else {
					log.error("Bad key/value in file[" + fileName + "] at line [" + (i+1) + "], detail:" + Arrays.deepToString(pair));
					properties.add(new Pair<String, String>("", null));
				}
			} else if (pair.length != 2) {
				log.error("Bad key/value in file[" + fileName + "] at line [" + (i+1) + "], detail:" + Arrays.deepToString(pair));
				properties.add(new Pair<String, String>("", null));
			} else {
				if(pair[0].matches(VALID_NAME)
						&& pair[1].matches(VALID_VALUE))
					properties.add(new Pair<String, String>(pair[0], pair[1]));
				else log.error("Property" + rawPair + " contains invalid character");
			}
		}
	}

	private void readFromConfig(Map<String, Object> container, Config other) {
		for (String key : other.keyList())
			putToMap(container, key, other.getProperty(key));
	}
	
	private void putToMap(Map<String, Object> map, String key, Object value) {
		map.put(key, value);
		keyList.add(key);
	}
	
	public List<String> keyList() {
		return new ArrayList<String>(keyList);
	}

	public boolean containsKey(String key) {
		if (key == null)
			throw new NullPointerException("Key is null");
		return dataMap.containsKey(key);
	}

	/**
	 * Get raw value by key
	 * @param key
	 * @throws
	 * LYException Generally means given key pairs no value.
	 */
	private Object getProperty(String key) {
		if (key == null)
			throw new LYException("Key is null");
		Object tmp = dataMap.get(key);
		if (tmp == null)
			throw new LYException("Follow entry[" + key + "] not found, check your config file[" + fileName + "]");
		return tmp;
	}
	
	/**
	 * Get value type of a key from config
	 * @param key
	 * @return
	 */
	public Class<?> getValueTypeByKey(String key) {
		return getProperty(key).getClass();
	}

	/**
	 * Get singleton object from config, it may also be a singleton-{@link String} or sub-config
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		return getProperty(key);
	}

	/**
	 * Create a new instance from config, entry with key <b>MUST</b> be a class name, and it <b>MUST</b> have a default constructor
	 * @param key
	 * @return
	 */
	public Object getNewInstance(String key) {
		try {
			String className = (String) getProperty(key);
			return Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new LYException("Can not make instance", e);
		}
	}
	
	/**
	 * Get sub-config from this config.
	 * <br><b>[!]</b>If this is a plain config, invoke it will surely result in an exception
	 * @param key
	 * @return A sub config
	 * @throws
	 * LYExceptin This entry is not a {@link Config}, this entry is not existed
	 */
	public Config getConfig(String key) {
		try {
			return (Config) getProperty(key);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] into Config");
		}
	}

	public String getString(String key) {
		return getProperty(key).toString();
	}

	public Short getShort(String key) {
		String value = getString(key);
		try {
			return Short.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}
	
	public Integer getInteger(String key) {
		String value = getString(key);
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Long getLong(String key) {
		String value = getString(key);
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Float getFloat(String key) {
		String value = getString(key);
		try {
			return Float.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Double getDouble(String key) {
		String value = getString(key);
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Boolean getBoolean(String key) {
		String value = getString(key);
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Byte getByte(String key) {
		String value = getString(key);
		try {
			return Byte.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Character getCharacter(String key) {
		String value = getString(key);
		try {
			return Character.valueOf(value.charAt(0));
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	@Override
	public String toString() {
		return "Config [dataMap=" + dataMap + ", keyList=" + keyList
				+ ", mode=" + mode + "]";
	}

}
