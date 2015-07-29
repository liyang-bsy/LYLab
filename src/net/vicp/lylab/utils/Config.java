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
 * Powerful configuration object for global uses
 * 
 * <br>Support follow mark
 * <br>
 * 
 * @author Young
 *
 */
public class Config extends NonCloneableBaseObject {
	// grammar, start with any thing except '[', and end with '[number]'
	// may used for future array/switch value support
	// Pattern.compile("^[^\\[]+[\\[][0-9]*[\\]]$").matcher("65435632[554234]").find()

	public static void main(String[] arg) {
		System.out.println(new Config(CoreDef.rootPath + "\\config\\A.txt"));
	}

	protected transient String fileName;
	protected Map<String, Object> dataMap;
	protected transient Config parent;
	protected int mode = 0;
	protected transient Stack<String> fileNameTrace;

	/**
	 *  Create an empty config
	 */
	public Config() {
		this(null, null, null);
	}

	public Config(String fileName) {
		this(fileName, new Stack<String>(), null);
	}

	public Config(String fileName, Stack<String> fileNameTrace, Config parent) {
		this.fileName = fileName;
		this.fileNameTrace = fileNameTrace;
		this.parent = parent;
		dataMap = new ConcurrentHashMap<String, Object>();
		reload();
	}

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
					// sub-configuration reference
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
						while(root != null && obj == null)
						{
							if(root.containsKey(propertyRealValue))
								obj = root.get(propertyRealValue);
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

	protected List<Pair<String, String>> loader() {
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

	public Object getObject(String key) {
		return getProperty(key);
	}

	public Object getNewInstance(String key) {
		try {
			String className = (String) getProperty(key);
			return Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new LYException("Can not make instance", e);
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

	public Config getConfig(String key) {
		try {
			return (Config) getProperty(key);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] into Config");
		}
	}

	@Override
	public String toString() {
		return "Config [dataMap=" + dataMap + ", mode=" + mode + "]";
	}

}
