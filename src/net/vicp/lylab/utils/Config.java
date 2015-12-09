package net.vicp.lylab.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.core.model.Pair;

/**
 * Powerful config object for global uses, support specific program mark
 * <br>
 * <br>Follow marks were supported
 * <br><br><b>Mode mark(first line only):</b> {@code [TREE]} or {@code [PLAIN]}, {@code [TREE]} is default
 * <br><br><b>Comment mark:</b> key(start with #) or bad key/value will be regard as comment
 * <br>Example:
 * <br>#comment = o(*≧▽≦)ツ
 * <br>Well, it will be nothing...
 * <br><br><b>Object mark:</b> the value of key(start with *) should be class name, stored its new instance. If value(start with &), will be regard as key and try to find any the key from itself or its parents.
 * <br>Example:
 * <br>*object1=com.java.ExampleClass
 * <br>You can access "object1" by {@link #getObject(String key)}: {@code config.getObject("object1")};
 * <br><b>[!]</b>ExampleClass <b>MUST</b> have a default constructor
 * <br><br><b>Parameter mark:</b> the value of key(start with ^) will be set to last Object(* mark). If value starts with &, will be regard as key and try to find any the key from itself or its parents.
 * <br>Example:
 * <br>^value1=123.45
 * <br>{@code exampleClass.setValue1(123.45);}
 * <br>Its key will finally be dropped, certainly never replace any existed keys
 * <br><br><b>Configuration mark:</b> key(start with $) will be regard as another {@link Config}
 * <br>Example:
 * <br>$config1=dir/next_config.txt
 * <br>You can access "object1" by {@link #getObject(String key)} or {@link #getConfig(String key)} : {@code config.getConfig("config1")};
 * <br>If sub-config is {@code [PLAIN]}, all its entries will be obtain into current config
 * <br><b>[!]</b>Especially, if the key start with "$+", all entries will be obtain into current config like PLAIN mode
 * <br><br><b>Array mark:</b> key(start with []) will be regard as an array
 * <br>Example:
 * <br>[]item=abc
 * <br>[]item=def
 * <br>You will get an {@link ArrayList} contains { "abc","def" };
 * <br>The key will be retained at current config, you may reference to it somewhere else.
 * <br><br><b>Extract mark:</b> the value(start by '&' and contains '->' mark) of key(start with ^/[]/*), is the reference value's getter.
 * <br>Example:
 * <br>*a=^b->c
 * <br>Then a=b.getC();
 * <br><br><b>Global switch value mark:</b> the value of key(start with !), will be regard as switch-value. You may use it by {@link #getString(String key)} or ":" grammar
 * <br>Example:
 * <br>!a=0
 * <br>b:0=apple
 * <br>b:1=pear
 * <br>Then 'b' is an "apple", you can also access 'a' by {@link #getString(String key)}, returns 0.
 * <br>Use 'switch-value' on basic type is highly recommended
 * <br>
 * <br><b>Different between Tree/Plain {@link Config}</b>
 * <br>Tree-Configuration {@link Config} could get it sub-config by {@link #getConfig(String key)}
 * <br>Plain-Configuration will obtain all sub-config into itself
 * <br>
 * <br><b>Basic entry rule:</b>
 * <br>Entry start with "#" will be regard as comment and ignored for dataMap;
 * <br>Key may start with function mark "$"/"*"/"^"/"[]", itself contains underline, number or alphabet
 * <br>value may start with function mark "&", itself may contain any visible character except "&"
 * 
 * @author Young
 * @since 2015.07.29
 * @version 2.1.0
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class Config extends NonCloneableBaseObject {
	public static void main(String[] args) {
		System.out.println(Utils.serialize(new Config("c:/config/config.txt").dataMap));
	}
	// Further multi-switch support will base on grammar "Key@switch:value=Value"

	private transient String fileName;
	private Map<String, Object> dataMap;
//	private Map<String, String> switches;
	private String globalSwitch = null;
	private Map<String, Boolean> watchList = new HashMap<String, Boolean>();
	private List<String> keyList = new ArrayList<String>();
	private transient Config parent;
	/**
	 * 0 is TREE
	 * 1 is PLAIN
	 */
	private int mode = 0;
	private transient Stack<String> fileNameTrace;
	private transient List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
	private transient List<Pair<String, String>> lazyLoad = new ArrayList<Pair<String, String>>();

	public static final String VALID_NAME = "(([*^!]|\\[\\]|\\$\\+|\\$)[_\\w]*)|(([*^!]|\\[\\]|\\$\\+|\\$)[_\\w]*:[_\\w]*)|([_\\w]*:[_\\w]*)|([_\\w]*)";
	public static final String VALID_VALUE = "([&][^&]*)|([^&]*)";

	public static final Map<String, Integer> sortRule = new HashMap<String, Integer>();
	public static final Set<String> lazyLoadSet;
	/**
	 * The higher, the later.
	 */
	static {
//		sortRule.put(":", 30);
		sortRule.put("*", 50);
		sortRule.put("[]", 50);
		sortRule.put("^", 50);
		sortRule.put("&", 80);
		sortRule.put("$", 100);
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
		dataMap = new ConcurrentHashMap<String, Object>();
		if(parent != null && parent.globalSwitch != null)
			globalSwitch = parent.globalSwitch;
//		switches = new HashMap<String, String>();
		reload();
	}

	/**
	 * You may reload your configuration manually, it will close all current 'opened' object.
	 * <br>[!]If you keep a Strong-Reference to an 'opened' Object, keep using it may result in an exception.
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
		deepClose();
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
				if (isLazyLoad(property))
					insertLazyLoad(property);
				else
					putToMap(dataMap, propertyName, property.getRight());
			} catch (Exception e) {
				throw new LYException("Failed to load config file[" + fileName
						+ "] at line [" + getLine(property) + "]", e);
			}
		}
		if (!lazyLoad.isEmpty())
			lazyLoad();
		if(watchList.containsValue(false))
			for (String key : watchList.keySet())
				if (!watchList.get(key))
					throw new LYException("Config file[" + fileName + "] contains undefined switch values:" + key);
		fileNameTrace.pop();
	}
	
	/**
	 * Call this without reasons will destroy <b>ALL</b> your current configuration.
	 * <br>It's advised to run this <b>ONLY IF</b> the server is determined to shutdown.
	 * <br>Anyhow, it's your own risk to call this!
	 */
	public void deepClose() {
		synchronized (lock) {
			for(String key:dataMap.keySet())
			{
				Object tmp = dataMap.get(key);
				if(tmp instanceof AutoCloseable)
					try {
						((AutoCloseable) tmp).close();
						log.info(tmp.getClass().getSimpleName() + " - Closed");
					} catch (Throwable t) {
						log.error(tmp.getClass().getSimpleName() + " - Close failed:" + Utils.getStringFromThrowable(t));
					}
				else if(tmp instanceof Config)
					((Config) tmp).deepClose();
			}
			dataMap.clear();
			keyList.clear();
		}
	}

	/**
	 * Returns true if this property should do lazy load.
	 * @return
	 * true if this property should do lazy load
	 */
	private boolean isLazyLoad(Pair<String, String> property) {
		for (String signal : lazyLoadSet) {
			if (property.getLeft().contains(signal) || property.getRight().contains(signal))
				return true;
		}
		return false;
	}

	// Simulate a safely insert-sort
	private void insertLazyLoad(Pair<String, String> property) {
		int i = 0;
		int min = checkLazyLoadMinValue(property);
		for (; i < lazyLoad.size(); i++)
			if (min < checkLazyLoadMinValue(lazyLoad.get(i)))
				break;
		lazyLoad.add(i, property);
		log.debug("[Transcribe]:" + property);
	}
	
	private int checkLazyLoadMinValue(Pair<String, String> property) {
		int min = Integer.MAX_VALUE;
		int value = Integer.MAX_VALUE;
		for (String rule : lazyLoadSet) {
			if (property.getLeft().contains(rule) || property.getRight().contains(rule)) {
				value = sortRule.get(rule);
				if (value < min)
					min = value;
			}
		}
		return min;
	}
	
	private String checkLazyLoadMinRule(Pair<String, String> property) {
		int min = Integer.MAX_VALUE;
		int value = Integer.MAX_VALUE;
		String signal = "";
		for (String rule : lazyLoadSet) {
			if (property.getLeft().contains(rule) || property.getRight().contains(rule)) {
				value = sortRule.get(rule);
				if (value < min) {
					min = value;
					signal = rule;
				}
			}
		}
		return signal;
	}

	private void lazyLoad() {
		List<Object> lazyInitialize = new ArrayList<>();
		Object lastObject = null;
		Pair<String, String> property = null;
		try {
			for (int i = 0; i < lazyLoad.size(); i++) {
				property = lazyLoad.get(i);
				log.debug("[Execute]:" + property);
				String propertyName = property.getLeft();
				String propertyValue = property.getRight();
				String rule = checkLazyLoadMinRule(property);
				switch (rule) {
				case "$": {
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
						int index1 = fileName.lastIndexOf("/");
						int index2 = fileName.lastIndexOf("\\");
						int index = (index1 > index2 ? index1 : index2);
						String filePath = fileName.substring(0, index + 1);
						testExist = new File(filePath + realFileName);
						if (testExist.exists() && testExist.isFile())
							realFileName = filePath + realFileName;
					}
					// if exist circle
					if (fileNameTrace.contains(realFileName))
						throw new LYException("Circle reference was found in config file[" + realFileName + "]");
					int inputMode = mode;
					if(propertyRealName.startsWith("+")) {
						propertyRealName = propertyRealName.substring(1);
						inputMode = 1;
					}
					switch (inputMode) {
					case 0:
						config = new Config(realFileName, fileNameTrace, this);
						putToMap(dataMap, propertyRealName, config);
						break;
					case 1:
						config = new Config(realFileName, fileNameTrace, this);
						readFromConfig(dataMap, config);
						break;
					default:
						throw new LYException("Unknow config mode: " + inputMode);
					}
				}
					break;
				case "*": {
					// object reference
					String propertyRealName = propertyName.substring(1);
					Object target = null;
					if (propertyValue.startsWith("&")) {
						Object obj = searchObjectReference(propertyValue.substring(1));
						target = obj;
					} else {
						target = Class.forName(propertyValue).newInstance();
					}
					putToMap(dataMap, propertyRealName, target);
					lazyInitialize.add(target);
					lastObject = target;
				}
					break;
				case "[]": {
					if (propertyName.charAt(1) != ']')
						throw new LYException("Bad array format ["
								+ propertyName + "], missing ']'");
					// array reference
					String propertyRealName = propertyName.substring(2);
					List valueContainer = (List) dataMap.get(propertyRealName);
					if (valueContainer == null)
						valueContainer = new ArrayList();
					if (propertyValue.startsWith("&")) {
						Object obj = searchObjectReference(propertyValue.substring(1));
						valueContainer.add(obj);
					} else
						valueContainer.add(propertyValue);
					putToMap(dataMap, propertyRealName, valueContainer);
				}
					break;
				case "^": {
					// object parameter reference
					String propertyRealName = propertyName.substring(1);
					if (propertyValue.startsWith("&")) {
						Object obj = searchObjectReference(propertyValue.substring(1));
						setter(lastObject, propertyRealName, obj);
					} else
						setter(lastObject, propertyRealName, propertyValue);
				}
					break;
//				case ":": {
//					// object parameter reference
//					String propertyRealName = propertyName.startsWith("*")?propertyName.substring(1):propertyName;
//					putToMap(dataMap, propertyRealName, propertyValue);
//				}
//					break;
				case "&": {
					// object parameter reference
					Object obj = searchObjectReference(propertyValue.substring(1));
					putToMap(dataMap, propertyName, obj);
				}
					break;
				default:
					throw new LYException("Unsupported grammar on property name:" + propertyName);
				}
			}
			for(Object obj:lazyInitialize)
				if(obj instanceof Initializable)
					try {
						((Initializable) obj).initialize();
						log.info(obj.getClass().getSimpleName() + " - Initialized");
					} catch (Throwable t) {
						log.error(Utils.getStringFromThrowable(t));
					}
		} catch (Exception e) {
			throw new LYException("Failed on lazy load config file[" + fileName
					+ "] at line [" + getLine(property) + "]", e);
		}
	}
	
	private Object searchObjectReference(String propertyRealValue) {
		String[] getters = new String[] {};
		if (propertyRealValue.contains("->")) {
			String[] valueFormat = propertyRealValue.split("->");
			getters = Arrays.copyOfRange(valueFormat, 1, valueFormat.length);
			propertyRealValue = valueFormat[0];
		}
		Object obj = null;
		Config root = this;
		while (true) {
			obj = root.dataMap.get(propertyRealValue);
			if (obj != null || root.parent == null)
				break;
			root = parent;
		}
		if (obj == null)
			throw new LYException("Cannot find reference [" + propertyRealValue + "] in this Config or parent Config");
		for (String getter : getters)
			obj = getter(obj, getter);
		return obj;
	}

	private int getLine(Pair<String, String> property) {
		for (int i = 0; i < properties.size(); i++)
			if (property.equals(properties.get(i)))
				return i + 1;
		return -1;
	}

	private void setter(Object owner, String fieldName, Object param)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		if (owner == null)
			throw new NullPointerException("Owner is null for setter[" + fieldName + "]");
		if (param == null)
			throw new NullPointerException("Parameter is null for setter[" + fieldName + "]");
		String setter = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		// Get setter by java.lang.reflect.*
		Method setMethod = null;
		Class<?> parameterClass = null;
		try {
			setMethod = owner.getClass().getDeclaredMethod(setter, String.class);
			parameterClass = String.class;
		} catch (Exception e) { }
		if (setMethod == null)
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
			if (param.getClass().getName().matches("^\\[L[a-zA-Z0-9_.]*;$")
					|| Collection.class.isAssignableFrom(param.getClass()))
				setMethod.invoke(owner, Caster.arrayCast((List<Object>) param, parameterClass));
			else if (param.getClass() == String.class)
				// But sometimes we may need casting parameter before invoking
				setMethod.invoke(owner, Caster.simpleCast((String) param, parameterClass));
			else
				setMethod.invoke(owner, param);
		else
			throw new LYException("No available setter[" + setter
					+ "] was found for " + owner.getClass());
	}

	private static Object getter(Object owner, String fieldName) {
		if (owner == null)
			throw new NullPointerException("Owner is null for getter[" + fieldName + "]");
		String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		// Get getter by java.lang.reflect.*
		try {
			Method getMethod = owner.getClass().getDeclaredMethod(getter);
			return getMethod.invoke(owner);
		} catch (Exception e) {
			throw new LYException("Invoke getter[" + getter + "] for " + owner.getClass() + "failed", e);
		}
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
					log.error("Ignored bad key/value in file[" + fileName
							+ "] at line [" + (i + 1) + "], detail:"
							+ Arrays.deepToString(pair));
					properties.add(new Pair<String, String>("", null));
				}
			} else if (pair.length != 2) {
				log.error("Ignored bad key/value in file[" + fileName
						+ "] at line [" + (i + 1) + "], detail:"
						+ Arrays.deepToString(pair));
				properties.add(new Pair<String, String>("", null));
			} else {
				if(pair[0].matches(VALID_NAME) && pair[1].matches(VALID_VALUE))
					properties.add(new Pair<String, String>(pair[0], pair[1]));
				else
					log.error("Ignored bad property [" + rawPair
							+ "] which contains invalid character");
			}
		}
	}

	private void readFromConfig(Map<String, Object> container, Config other) {
		for (String key : other.keyList())
			putToMap(container, key, other.getProperty(key));
	}

	private void putToMap(Map map, String key, Object value) {
		if (key.startsWith("!")) {
			// switch reference
			key = key.substring(1);
			if(globalSwitch == null)
				globalSwitch = value.toString();
			else throw new LYException("Global switch was redefined");
		}
		if (key.contains(":")) {
			String[] diff = key.split(":");
			key = diff[0];
			String switchValue = diff[1];
			if (globalSwitch == null)
				throw new LYException("Global switch hasn't been defined yet");
			if (globalSwitch.equals(switchValue))
				watchList.put(key, true);
			else {
				if (!watchList.containsKey(key))
					watchList.put(key, false);
				return;
			}
		}
//		if (map.containsKey(key))
//			throw new LYException("Duplicated key[" + key + "] in file[" + fileName + "]");
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
			throw new LYException("Entry[" + key + "] not found in your config file[" + fileName + "]");
		return tmp;
	}
	
	/**
	 * Get value type of a key from config
	 * @param key
	 * @return
	 * A Class<?> object expected.
	 * @throws LYException
	 * If key not existed, contains information about key/file name etc.
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
			throw new LYException("Convert entry[" + key + "] into Config", e);
		}
	}

	public <T> T[] getArray(String key, Class<T[]> arrayClass) {
		Object value = getProperty(key);
		try {
			return (T[]) Caster.arrayCast((List) value, arrayClass);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Object[] getArray(String key) {
		Object value = getProperty(key);
		try {
			return (Object[]) Caster.arrayCast((List) value, Object[].class);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public List getList(String key) {
		Object value = getProperty(key);
		try {
			return (List) Caster.arrayCast((List) value, ArrayList.class);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Set getSet(String key) {
		Object value = getProperty(key);
		try {
			return (Set) Caster.arrayCast((List) value, HashSet.class);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
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
					+ "] failed", e);
		}
	}

	public Integer getInteger(String key) {
		String value = getString(key);
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Long getLong(String key) {
		String value = getString(key);
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Float getFloat(String key) {
		String value = getString(key);
		try {
			return Float.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Double getDouble(String key) {
		String value = getString(key);
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Boolean getBoolean(String key) {
		String value = getString(key);
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Byte getByte(String key) {
		String value = getString(key);
		try {
			return Byte.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	public Character getCharacter(String key) {
		String value = getString(key);
		try {
			return Character.valueOf(value.charAt(0));
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed", e);
		}
	}

	@Override
	public String toString() {
		return "Config [dataMap=" + dataMap + ", keyList=" + keyList
				+ ", mode=" + mode + "]";
	}

}
