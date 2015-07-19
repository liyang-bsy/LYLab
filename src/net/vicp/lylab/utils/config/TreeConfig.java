package net.vicp.lylab.utils.config;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.model.Pair;

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
		List<Pair<String, String>> pairs = loader();
		Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();
		for (Pair<String, String> property : pairs) {
			try {
				String propertyName = property.getLeft().trim();
				if (propertyName.equals("") || propertyName.startsWith("#"))
					continue;
				String propertyValue = property.getRight().trim();
				if (propertyName.startsWith("$")) {
					String propertyRealName = propertyName.substring(1).trim();
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
