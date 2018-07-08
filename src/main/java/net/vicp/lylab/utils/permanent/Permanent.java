package net.vicp.lylab.utils.permanent;

import java.util.List;

import net.vicp.lylab.core.NonCloneableBaseObject;

public abstract class Permanent extends NonCloneableBaseObject {
	protected String fileName;
	protected String instanceClassName;
	
	public abstract List<Object> readFromDisk();
	public abstract void saveToDisk(Iterable<?> container);

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getInstanceClassName() {
		return instanceClassName;
	}

	public void setInstanceClassName(String instanceClassName) {
		this.instanceClassName = instanceClassName;
	}

}
