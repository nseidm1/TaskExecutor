package main.taskexecutor;

import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * @author nseidm1
 * 
 */
public abstract class TaskExecutorActivity extends FragmentActivity implements TaskCompletedCallback,
		TasksRestoredCallback, TaskExecutorReferenceCallback
{
	protected TaskExecutor mTaskExecutor;

	public abstract boolean shouldPauseAndResumeWithActivity();

	@Override
	public void onPause()
	{
		super.onPause();
		if (shouldPauseAndResumeWithActivity())
			mTaskExecutor.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		TaskExecutorService.requestExecutorReference(this, this, this);
	}

	@Override
	public void getTaskExecutorReference(TaskExecutor taskExecutor)
	{
		mTaskExecutor = taskExecutor;
		if (shouldPauseAndResumeWithActivity())
			mTaskExecutor.onResume(this);
	}

	@Override
	public void tasksHaveBeenRestored()
	{
		mTaskExecutor.onResume(this);
		mTaskExecutor.executeQueue();
	}
}