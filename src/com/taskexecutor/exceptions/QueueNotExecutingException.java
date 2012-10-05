package com.taskexecutor.exceptions;

public class QueueNotExecutingException extends Exception
{
	private static final long serialVersionUID = 5595613916707040921L;

	public QueueNotExecutingException(String message)
	{
		super(message);
	}
}
