package com.taskexecutor.Helpers;

import java.util.ArrayList;
import android.os.Handler;
import com.taskexecutor.TaskExecutor;
import com.taskexecutor.callbacks.TaskCompletedCallback;
import com.taskexecutor.runnables.Task;

/**
 * @author nseidm1
 *
 */
public class QueueHelper
{
	public static void setCallbackForAllQueuedTasks(ArrayList<Task> queue, TaskCompletedCallback completeCallback)
	{
		for (Task task : queue)
		{
			task.setCompleteCallback(completeCallback);
		}
	}

	public static void setTaskExecutorForAllQueuedTasks(ArrayList<Task> queue, TaskExecutor taskExecutor)
	{
		for (Task task : queue)
		{
			task.setTaskExecutor(taskExecutor);
		}
	}

	public static void setUIHandlerForAllQueuedTask(ArrayList<Task> queue, Handler uiHandler)
	{
		for (Task task : queue)
		{
			task.setUiHandler(uiHandler);
		}
	}
}