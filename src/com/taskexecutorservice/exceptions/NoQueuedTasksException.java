package com.taskexecutorservice.exceptions;

public class NoQueuedTasksException extends Exception {
	private static final long serialVersionUID = -6857068538894375559L;

	public NoQueuedTasksException(String message) {
		super(message);
	}
}
