package com.king.tratt;

public class TdlFileFieldErrorDescriptor {
	public String errorDescription;
	public String errorNodePath;

    TdlFileFieldErrorDescriptor(String errorDescription, String errorNodePath) {
		this.errorDescription = errorDescription;
		this.errorNodePath = errorNodePath;
	}

    String getErrorDescription() {
		return errorDescription;
	}

    String getErrorNodePath() {
		return errorNodePath;
	}
}
