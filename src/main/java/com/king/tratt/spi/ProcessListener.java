package com.king.tratt.spi;

public interface ProcessListener<E extends Event> {

	public enum EmitType {
		START, END, EMIT, INVALID, INVALID_STATE, TIMEOUT
	}

	void onStart();
	void onEnd();
	void onSuccess();
	void onFailure();
	void onTimeOut();
	void onMatch();

}
