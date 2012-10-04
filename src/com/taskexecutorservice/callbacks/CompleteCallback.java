package com.taskexecutorservice.callbacks;

import com.taskexecutorservice.runnables.Task;

public interface CompleteCallback {
	public void onTaskCompletion(Task task, boolean success, Exception exception);
}
