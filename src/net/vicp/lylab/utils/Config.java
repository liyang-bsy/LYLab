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

	public Config() {
	}

	public Config(String fileName) {
		this.fileName = fileName;
		load();
	}

	private volatile String fileName;
	private volatile Map<String, String> dataMap;

	public Set<String> keySet()
	{
		if (dataMap == null)
			throw new LYException("Raw config, please load or reload");
		return dataMap.keySet();
	}
	
	public String getProperty(String key) {
		if (key == null)
			throw new LYException("Key is null");
		if (dataMap == null)
			throw new LYException("Raw config, please load or reload");
		String tmp = dataMap.get(key);
		if (tmp == null)
			throw new LYException("Follow entry[" + key
					+ "] not such, check your config file[" + fileName + "]");
		return dataMap.get(key);
	}

	public String getString(String key) {
		return getProperty(key);
	}

	public Integer getInteger(String key) {
		String value = getProperty(key);
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Double getDouble(String key) {
		String value = getProperty(key);
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Boolean getBoolean(String key) {
		String value = getProperty(key);
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public Long getLong(String key) {
		String value = getProperty(key);
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			throw new LYException("Convert entry[" + key + "] value[" + value
					+ "] failed");
		}
	}

	public synchronized void load() {
		if (dataMap != null || fileName == null)
			return;
		File file = new File(fileName);
		Properties p = new Properties();
		try {
			if (!file.exists())
				throw new FileNotFoundException();
			InputStream inputStream = new FileInputStream(file);
			p.load(inputStream);
		} catch (Exception e) {
			throw new LYException("Failed to load file with below path:\n" + fileName, e);
		}
		Map<String, String> tmp = new ConcurrentHashMap<String, String>();
		for (String propertyName : p.stringPropertyNames()) {
			propertyName = propertyName.trim();
			if (propertyName.startsWith("#"))
				continue;
			tmp.put(propertyName, p.getProperty(propertyName).trim());
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
