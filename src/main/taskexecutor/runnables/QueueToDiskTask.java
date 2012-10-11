package main.taskexecutor.runnables;

import java.io.IOException;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.TaskExecutorService;
import main.taskexecutor.helpers.QueueOnDiskHelper;
import android.util.Log;

public class QueueToDiskTask implements Runnable
{
	private TaskExecutor mTaskExecutor;
	private TaskExecutorService mTaskExecutorService;

	public QueueToDiskTask(TaskExecutor taskExecutor, TaskExecutorService taskExecutorService)
	{
		mTaskExecutor = taskExecutor;
		mTaskExecutorService = taskExecutorService;
	}

	@Override
	public void run()
	{
		updateTasksOnDisk();
	}

	private void updateTasksOnDisk()
	{
		try
		{
			QueueOnDiskHelper.updateTasksOnDisk(mTaskExecutorService, mTaskExecutor);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.e(TaskExecutorService.class.getName(), "Error saving existing queue.");
		}
	}
}