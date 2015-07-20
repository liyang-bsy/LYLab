package net.vicp.lylab.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.model.Pair;

public class Config extends NonCloneableBaseObject {
	protected String fileName;
	protected Map<String, Object> dataMap;
	protected transient Config parent;
	protected int mode = 0;
	protected transient Stack<String> fileNameTrace;
	
	public Config(String fileName) {
		this(fileName, new Stack<String>(), null);
	}

	public Config(String fileName, Stack<String> fileNameTrace, Config parent) {
		this.fileName = fileName;
		this.fileNameTrace = fileNameTrace;
		this.parent = parent;
		reload();
	}

	public synchronized void reload() {
		if (fileName == null)
			return;
		fileNameTrace.push(fileName);
		int line = 0;
		List<Pair<String, String>> pairs = loader();
		Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();
		for (int i = 0; i < pairs.size(); i++) {
			Pair<String, String> property = pairs.get(i);
			try {
				String propertyName = property.getLeft().replaceAll("[\u0000-\u0020]", "");//.trim();
				if (propertyName.equals("") || propertyName.startsWith("#"))
					continue;
				if(i == 0 && property.getRight() == null) {
					if(propertyName.equals("[TREE]"))
						mode = 0;
					if(propertyName.equals("[PLAIN]"))
						mode = 1;
					continue;
				}
				String propertyValue = property.getRight().replaceAll("[\u0000-\u0020]", "");//.trim();
				if (propertyName.startsWith("$")) {
					String propertyRealName = propertyName.substring(1).replaceAll("[\u0000-\u0020]", "");//.trim();
					String realFileName = propertyValue;
					Config config = null;
					testfile: {
						File testExist = null;
						testExist = new File(realFileName);
						if(testExist.exists() && testExist.isFile())
							break testfile;
						int index = fileName.lastIndexOf(File.separator);
						String filePath = fileName.substring(0, index + 1);
						testExist = new File(filePath + realFileName);
						if(testExist.exists() && testExist.isFile())
							realFileName = filePath + realFileName;
					}
					if(fileNameTrace.contains(realFileName))
						throw new LYException("It looks like there was a reference circle in config file[" + realFileName + "]");
					switch (mode) {
					case 0:
						config = new Config(realFileName, fileNameTrace, this);
						tmp.put(propertyRealName, config);
						break;
					case 1:
						config = new Config(realFileName, fileNameTrace, this);
						readFromConfig(tmp, config);
						break;
					default:
						break;
					}
				} else
					tmp.put(propertyName, propertyValue);
			} catch (Exception e) {
				throw new LYException("Failed to load config file[" + fileName + "] at line " + line, e);
			}
			line++;
		}
		dataMap = tmp;
		tmp = null;
		fileNameTrace.pop();
	}
	
	protected List<Pair<String, String>> loader() {
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		List<String> rawList = Utils.readFileByLines(fileName);
		for (int i = 0; i < rawList.size(); i++) {
			String rawPair = rawList.get(i);
			String[] pair = rawPair.split("=");
			if (i == 0 && pair.length == 1) {
				if (pair[0].startsWith("[") && pair[0].endsWith("]")) {
					pairs.add(new Pair<String, String>(pair[0], null));
					continue;
				}
				log.error("Bad key/value" + Arrays.deepToString(pair));
				continue;
			}
			if (pair.length != 2) {
				log.error("Bad key/value" + Arrays.deepToString(pair));
				continue;
			}
			pairs.add(new Pair<String, String>(pair[0], pair[1]));
		}
		return pairs;
	}
	
	private void readFromConfig(Map<String, Object> container, Config other)
	{
		for (String key : other.keySet()) {
			container.put(key, other.getProperty(key));
		}
	}
	
	public Set<String> keySet()
	{
		return dataMap.keySet();
	}
	
	public Object getProperty(String key) {
		if (key == null)
			throw new LYException("Key is null");
		Object tmp = dataMap.get(key);
		if (tmp == null)
			throw new LYException("Follow entry[" + key
					+ "] not found, check your config file[" + fileName + "]");
		return tmp;
	}

	public Object getInstance(String key) {
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Config getParent() {
		return parent;
	}

	public void setParent(Config parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Config [fileName=" + fileName + ", dataMap=" + dataMap
				+ ", mode=" + mode + "]";
	}

}
