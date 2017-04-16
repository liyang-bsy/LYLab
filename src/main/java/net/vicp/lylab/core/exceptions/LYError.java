package net.vicp.lylab.core.exceptions;

public class LYError extends Error {
	private static final long serialVersionUID = -8672577910978450074L;

	int code;

	public LYError(String message) {
		this(-1, message);
	}
	
	public LYError(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
