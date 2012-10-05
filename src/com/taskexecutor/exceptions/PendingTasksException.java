package com.taskexecutor.exceptions;

public class PendingTasksException extends Exception {
	private static final long serialVersionUID = -2384788220358335545L;

	public PendingTasksException(String message) {
		super(message);
	}
}
