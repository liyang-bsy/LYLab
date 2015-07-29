package net.vicp.lylab.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.model.Pair;

/**
 * Powerful config object for global uses, support specific program mark
 * <br>
 * <br>Follow marks were supported
 * <br><br><b>Mode mark(first line only):</b> {@code [TREE]} or {@code [PLAIN]}, {@code [TREE]} is default
 * <br>Comment mark: key(start with #) or bad key/value will be regard as comment
 * <br>Example:#comment = o(*≧▽≦)ツ
 * <br>Well, it will be nothing...
 * <br><br><b>Object mark:</b> the value of key(start with *) should be class name, stored its new instance
 * <br>Example:*object1=net.vicp.lylab.utils.ExampleClass
 * <br>You can access "object1" by {@link #getObject(String key)}: {@code config.getObject("object1")};
 * <br><b>[!]</b>ExampleClass <b>MUST</b> have a default constructor
 * <br><br><b>Parameter mark:</b> the value of key(start with ^) will be set to last Object(* mark), if value(start with &), will be regard as key and try to find any the key from itself or its parents.
 * <br>Example:^value1=123.45
 * <br>{@code exampleClass.setValue1(123.45);}
 * <br>Its key will be finally dropped
 * <br><br><b>Configuration mark:</b> key(start with $) will be regard as another {@link Config}
 * <br>Example:$config1=dir/next_config.txt
 * <br>You can access "object1" by {@link #getObject(String key)} or {@link #getConfig(String key)} : {@code config.getConfig("config1")};
 * <br>If sub-config is [PLAIN], all config entry will be obtain into current config
 * <br>
 * <br>Different between Tree/Plain {@link Config}
 * <br>Tree-Configuration {@link Config} could get it sub-config by {@link #getConfig(String key)}
 * <br>Plain-Configuration will obtain all sub-config into itself
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
	private transient Config parent;
	private int mode = 0;
	private transient Stack<String> fileNameTrace;

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
		int line = 0;
		Object lastObject = null;
		// load key/value for loader
		List<Pair<String, String>> pairs = loader();
		for (int i = 0; i < pairs.size(); i++) {
			Pair<String, String> property = pairs.get(i);
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
						throw new LYException("Circle reference was found in config file[" + realFileName + "]");
					switch (mode) {
					case 0:
						config = new Config(realFileName, fileNameTrace, this);
						dataMap.put(propertyRealName, config);
						break;
					case 1:
						config = new Config(realFileName, fileNameTrace, this);
						readFromConfig(dataMap, config);
						break;
					default:
						throw new LYException("Unknow config mode: " + mode);
					}
				} else if (propertyName.startsWith("*")) {
					// object reference
					String propertyRealName = propertyName.substring(1);
					Object target = Class.forName(propertyValue).newInstance();
					dataMap.put(propertyRealName, target);
					lastObject = target;
				} else if (propertyName.startsWith("^")) {
					// object parameter reference
					String propertyRealName = propertyName.substring(1);
					if (propertyValue.startsWith("&")) {
						String propertyRealValue = propertyValue.substring(1);
						Object obj = null;
						Map<String, Object> root = dataMap;
						while(true) {
							obj = root.get(propertyRealValue);
							if(obj != null || parent == null)
								break;
							root = parent.dataMap;
						}
						if(obj == null)
							throw new LYException("Cannot find reference [" + propertyRealValue + "] in this Config or parent Config");
						setter(lastObject, propertyRealName, obj);
					} else
						setter(lastObject, propertyRealName, propertyValue);
				} else
					dataMap.put(propertyName, propertyValue);
			} catch (Exception e) {
				throw new LYException("Failed to load config file[" + fileName
						+ "] at line [" + line + "]", e);
			}
			line++;
		}
		fileNameTrace.pop();
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
		for (Method method : owner.getClass().getDeclaredMethods()) {
			if (method.getName().equals(setter)) {
				Class<?> parameterClass = method.getParameterTypes()[0];
				if (param.getClass() != String.class)
					method.invoke(owner, param);
				else
					method.invoke(owner,Caster.simpleCast(param, parameterClass));
				break;
			}
		}
	}

	private List<Pair<String, String>> loader() {
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		List<String> rawList = Utils.readFileByLines(fileName, false);
		for (int i = 0; i < rawList.size(); i++) {
			// trim
			String rawPair = rawList.get(i).replaceAll("[\u0000-\u0020]", "");

			String[] pair = rawPair.split("=");
			if (pair.length == 1) {
				if (pair[0].equals("") || pair[0].startsWith("#") || (i == 0 && pair[0].startsWith("[") && pair[0].endsWith("]"))) {
					pairs.add(new Pair<String, String>(pair[0], null));
				} else {
					log.error("Bad key/value at line [" + (i+1) + "], detail:" + Arrays.deepToString(pair));
					pairs.add(new Pair<String, String>("#", null));
				}
			}
			else if (pair.length != 2) {
				log.error("Bad key/value at line [" + (i+1) + "], detail:" + Arrays.deepToString(pair));
				pairs.add(new Pair<String, String>("#", null));
			}
			else pairs.add(new Pair<String, String>(pair[0], pair[1]));
		}
		return pairs;
	}

	private void readFromConfig(Map<String, Object> container, Config other) {
		for (String key : other.keySet()) {
			container.put(key, other.getProperty(key));
		}
	}

	public Set<String> keySet() {
		return dataMap.keySet();
	}

	public boolean containsKey(String key) {
		if (key == null)
			throw new NullPointerException("Key is null");
		return dataMap.containsKey(key);
	}

	private Object getProperty(String key) {
		if (key == null)
			throw new LYException("Key is null");
		Object tmp = dataMap.get(key);
		if (tmp == null)
			throw new LYException("Follow entry[" + key + "] not found, check your config file[" + fileName + "]");
		return tmp;
	}

	/**
	 * Get raw object from config
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		return getProperty(key);
	}

	/**
	 * Get new instance from config, entry with key <b>MUST</b> be a class name, and it <b>MUST</b> have a default constructor
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
	 * @return
	 * A sub config
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

	public Integer getInteger(String key) {
		String value = getString(key);
		try {
			return Integer.valueOf(value);
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

	public Long getLong(String key) {
		String value = getString(key);
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	@Override
	public String toString() {
		return "Config [dataMap=" + dataMap + ", mode=" + mode + "]";
	}

}
