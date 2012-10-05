package com.taskexecutor;

import android.support.v4.app.FragmentActivity;

import com.taskexecutor.callbacks.CompleteCallback;

public abstract class TaskExecutorActivity extends FragmentActivity implements CompleteCallback
{
	protected TaskExecutor mTaskExecutor = TaskExecutor.getInstance();

	@Override
	public void onResume()
	{
		super.onResume();
		if (mTaskExecutor.getQueueCount() != 0)
			mTaskExecutor.setCallbackForAllQueuedTasks(this);
		if (mTaskExecutor.isPaused())
			mTaskExecutor.resume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (!mTaskExecutor.getShouldContinueExecutionIfPaused() && mTaskExecutor.isExecuting() && !mTaskExecutor.isPaused())
			mTaskExecutor.pause();
	}

}
