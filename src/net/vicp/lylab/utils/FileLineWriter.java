package net.vicp.lylab.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;

public final class FileLineWriter<T> extends NonCloneableBaseObject {
	/**
	 * 路径
	 */
	private String path;
	/**
	 * 后缀
	 */
	private String postfix = "txt";
	/**
	 * 最大输出条数
	 */
	private int maxLine = 10000;
	/**
	 * 文件名
	 */
	private String fileName;
	/**
	 * 文件输出
	 */
	private FileOutputStream fileOut = null;
	/**
	 * 输出条数
	 */
	private int outCount = 0;

	public FileLineWriter(String path) {
		this.path = path;
		Utils.createDirectory(path);
	}

	public void writeLine(List<T> lines) {
		synchronized (lock) {
			try {
				for (T tmp : lines)
				{
					if(isOpenFile() == false)
						open();
					fileOut.write(Utils.serialize(tmp).getBytes(CoreDef.CHARSET));
					fileOut.write("\r\n".getBytes());
					outCount++;
					if (maxLine <= outCount)
						close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writeLine(T data) {
		synchronized (lock) {
			try {
				if(isOpenFile() == false)
					open();
				fileOut.write(Utils.serialize(data).getBytes(CoreDef.CHARSET));
				fileOut.write("\r\n".getBytes());
				outCount++;
				if (maxLine <= outCount)
					close();
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
				fileOut.write((line + "\r\n").getBytes(CoreDef.CHARSET));
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
							postfix);
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

	public String getPostfix() {
		return postfix;
	}

	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	public int getMaxLine() {
		return maxLine;
	}

	public void setMaxLine(int maxLine) {
		this.maxLine = maxLine;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
