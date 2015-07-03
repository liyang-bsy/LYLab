package net.vicp.lylab.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.exception.LYException;

public final class TreeConfig extends Config {
	public TreeConfig(String fileName) {
		super(fileName);
	}

	public TreeConfig(String fileName, Stack<String> fileNameTrace, Config parent) {
		super(fileName, fileNameTrace, parent);
	}
	
	public synchronized void reload() {
		if (fileName == null)
			return;
		fileNameTrace.push(fileName);
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
					if(fileNameTrace.contains(realFileName))
						throw new LYException("It looks like there was a reference circle in config file[" + realFileName + "]");
					config = new TreeConfig(realFileName, fileNameTrace, this);
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
		fileNameTrace.pop();
	}

}
