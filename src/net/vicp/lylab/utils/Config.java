package net.vicp.lylab.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;

public final class Config extends NonCloneableBaseObject {

	public Config(String fileName) {
		this.fileName = fileName;
		reload();
	}

	private volatile String fileName;
	private volatile Map<String, Object> dataMap;

	public Set<String> keySet()
	{
		if (dataMap == null)
			throw new LYException("Raw config, please load or reload");
		return dataMap.keySet();
	}
	
	public Object getProperty(String key) {
		if (key == null)
			throw new LYException("Key is null");
		if (dataMap == null)
			throw new LYException("Raw config, please load or reload");
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
		Object value = getProperty(key);
		try {
			return (Integer) value;
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Double getDouble(String key) {
		Object value = getProperty(key);
		try {
			return (Double) value;
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Boolean getBoolean(String key) {
		Object value = getProperty(key);
		try {
			return (Boolean) value;
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Long getLong(String key) {
		Object value = getProperty(key);
		try {
			return (Long) value;
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
				if (propertyName.equals(""))
					continue;
				if (propertyName.startsWith("#"))
					continue;
				String propertyValue = p.getProperty(propertyName).trim();
				if (propertyName.startsWith("$")) {
					String propertyRealName = propertyName.substring(1);
					Config config = null;
					if(propertyValue.contains(File.separator))
						config = new Config(propertyValue);
					else
					{
						int index = fileName.lastIndexOf(File.separator);
						String realFileName = fileName.substring(0, index + 1) + propertyValue;
						config = new Config(realFileName);
					}
					tmp.put(propertyRealName, config);
				} else
					tmp.put(propertyName, propertyValue);
			} catch (Exception e) {
				throw new LYException("Failed to load config file at line "
						+ line, e);
			}
		}
		dataMap = tmp;
		tmp = null;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
