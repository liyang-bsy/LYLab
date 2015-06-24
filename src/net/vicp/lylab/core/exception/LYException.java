package net.vicp.lylab.core.exception;

public class LYException extends RuntimeException {
	private static final long serialVersionUID = -8672577910978450074L;

	int code;

	public LYException(String message) {
		this(0, message);
	}
	
	public LYException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public LYException(String message, Throwable e) {
		this(0, message, e);
	}
	
	public LYException(int code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
