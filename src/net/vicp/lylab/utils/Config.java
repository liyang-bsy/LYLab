package net.vicp.lylab.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.BaseObject;
import net.vicp.lylab.core.exception.LYException;

public final class Config extends BaseObject {

	public Config() { }
	
	public Config(String fileName)
	{
		this.fileName = fileName;
		reload();
	}

	private volatile String fileName;
	private volatile Map<String, String> dataMap;
	
	public String getProperty(String key)
	{
		if(dataMap == null) throw new LYException("Raw config, please reload");
		return dataMap.get(key);
	}

	public synchronized void reload() {
		if(dataMap != null || fileName == null) return;
		File file = new File(fileName);
		Properties p = new Properties();
		try {
			if(!file.exists()) throw new FileNotFoundException();
			InputStream inputStream = new FileInputStream(file);
			p.load(inputStream);
		} catch (Exception e) {
			throw new LYException("Failed to load file with below path:\n" + fileName, e);
		}
		Map<String, String> tmp = new ConcurrentHashMap<String, String>();
		for(String propertyName:p.stringPropertyNames())
		{
			propertyName = propertyName.trim();
			if(propertyName.startsWith("#")) continue;
			tmp.put(propertyName, p.getProperty(propertyName));
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
