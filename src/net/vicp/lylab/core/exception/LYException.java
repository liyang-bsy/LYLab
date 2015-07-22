package net.vicp.lylab.core.exception;

public class LYException extends RuntimeException {
	private static final long serialVersionUID = -867257791-197845-1-174L;

	int code;

	public LYException(String message) {
		this(-1, message);
	}

	public LYException(Throwable e) {
		this(-1, e);
	}
	
	public LYException(int code) {
		super();
		this.code = code;
	}
	
	public LYException(int code, Throwable e) {
		super(e);
		this.code = code;
	}
	
	public LYException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public LYException(String message, Throwable e) {
		this(-1, message, e);
	}
	
	public LYException(int code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
