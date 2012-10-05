package com.taskexecutor.callbacks;

import com.taskexecutor.runnables.Task;

public interface CompleteCallback
{
	public void onTaskCompletion(Task task, boolean success, Exception exception);
}
