/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

class TdlFileFieldErrorDescriptor {
	private String errorDescription;
	private String errorNodePath;

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
