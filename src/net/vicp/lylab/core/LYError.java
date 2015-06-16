package net.vicp.lylab.core;

public class LYError extends RuntimeException {
	private static final long serialVersionUID = -8672577910978450074L;

	int code;

	public LYError(String message) {
		this(0, message);
	}
	
	public LYError(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
