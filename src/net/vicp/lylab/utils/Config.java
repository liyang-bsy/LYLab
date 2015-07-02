package net.vicp.lylab.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;

public final class Config extends NonCloneableBaseObject {

	Stack<String> fileNameStack;
	
	public Config(String fileName) {
		this(fileName, new Stack<String>());
	}

	public Config(String fileName, Stack<String> fileNameStack) {
		this.fileName = fileName;
		this.fileNameStack = fileNameStack;
		reload();
	}

	private volatile String fileName;
	private volatile Map<String, Object> dataMap;

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
		return dataMap.get(key);
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

	public synchronized void reload() {
		if (fileName == null)
			return;
		fileNameStack.push(fileName);
		int line = 0;
		File file = new File(fileName);
		Properties p = new Properties();
		try {
			if (!file.exists())
				throw new FileNotFoundException();
			InputStream inputStream = new FileInputStream(file);
			p.load(inputStream);
		} catch (Exception e) {
			throw new LYException("Failed to load config file with below path:\n" + fileName, e);
		}
		Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();
		for (String propertyName : p.stringPropertyNames()) {
			try {
				propertyName = propertyName.trim();
				if (propertyName.equals("") || propertyName.startsWith("#"))
					continue;
				String propertyValue = p.getProperty(propertyName).trim();
				if (propertyName.startsWith("$")) {
					String propertyRealName = propertyName.substring(1);
					String realFileName = propertyValue;
					Config config = null;
					if(!realFileName.contains(File.separator)) {
						int index = fileName.lastIndexOf(File.separator);
						String filePath = fileName.substring(0, index + 1);
						realFileName = filePath + realFileName;
					}
					if(fileNameStack.contains(realFileName))
						throw new LYException("It looks like there was a reference circle in config file[" + realFileName + "]");
					config = new Config(realFileName, fileNameStack);
					tmp.put(propertyRealName, config);
				} else
					tmp.put(propertyName, propertyValue);
			} catch (Exception e) {
				throw new LYException("Failed to load config file[" + fileName + "] at line " + line, e);
			}
			line++;
		}
		dataMap = tmp;
		tmp = null;
		fileNameStack.pop();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
