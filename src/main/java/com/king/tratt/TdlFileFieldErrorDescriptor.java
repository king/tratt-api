package com.king.tratt;

public class TdlFileFieldErrorDescriptor {
	private String errorDescription;
	private String errorNodePath;
	public TdlFileFieldErrorDescriptor(String errorDescription,
			String errorNodePath) {
		super();
		this.errorDescription = errorDescription;
		this.errorNodePath = errorNodePath;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public String getErrorNodePath() {
		return errorNodePath;
	}
}
