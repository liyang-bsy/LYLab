package net.vicp.lylab.utils.permanent;

import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;

public abstract class Permanent extends NonCloneableBaseObject {
	protected String fileName;
	
	public abstract List<Object> readFromDisk();
	public abstract void saveToDisk(Iterable<?> container);

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
