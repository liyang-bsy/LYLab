package net.vicp.lylab.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public final class TextWriter extends NonCloneableBaseObject {
	private String path = "";
	private String suffix = "txt";
	private int maxLine = 10000;
	private String fileName;
	private FileOutputStream fileOut = null;
	private int outCount = 0;

	public TextWriter(String path) {
		this.path = path;
		Utils.createDirectory(path);
	}

	public void writeLine(List<String> lines) {
		synchronized (lock) {
			try {
				for (String line : lines)
				{
					if(isOpenFile() == false)
						open();
					fileOut.write((line + "\r\n").getBytes(CoreDef.CHARSET()));
					outCount++;
					if (maxLine <= outCount)
						close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writeLine(String line) {
		synchronized (lock) {
			try {
				if(isOpenFile() == false)
					open();
				fileOut.write((line + "\r\n").getBytes(CoreDef.CHARSET()));
				outCount++;
				if (maxLine <= outCount)
					close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isOpenFile() {
		if (fileOut == null)
			return false;
		return true;
	}

	public void open() {
		try {
			close();
			File file = new File(path + "/");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileName = path + "/" + System.currentTimeMillis() + ".temp";
			file = new File(fileName);
			if (file.exists())
				throw new LYException("File existed, can not over write");
			file.createNewFile();
			fileOut = new FileOutputStream(file, true);
			outCount = 0;
		} catch (Exception e) {
			throw new LYException("File open failed", e);
		}
	}

	public void close() {
		synchronized (lock) {
			try {
				if (fileOut != null) {
					fileOut.close();
					fileOut = null;
					File file = new File(fileName);
					String savedFileName = fileName.replaceFirst(
							"(?s)" + ".temp" + "(?!.*?" + ".temp" + ")",
							"." + suffix);
					file.renameTo(new File(savedFileName));
					outCount = 0;
					fileName = "";
				}
			} catch (Exception e) {
				fileOut = null;
				outCount = 0;
				fileName = "";
			}
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix.replaceFirst("^\\.", "");
	}

	public int getMaxLine() {
		return maxLine;
	}

	public void setMaxLine(int maxLine) {
		this.maxLine = maxLine;
	}

	public int getOutCount() {
		return outCount;
	}

}
