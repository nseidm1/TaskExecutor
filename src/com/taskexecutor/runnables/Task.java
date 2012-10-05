package com.taskexecutor.runnables;

import java.util.concurrent.Semaphore;

import android.os.Handler;

import com.taskexecutor.TaskExecutor;
import com.taskexecutor.callbacks.CompleteCallback;

public abstract class Task implements Runnable
{
	private TaskExecutor mTaskExecutor;
	private CompleteCallback mCompleteCallback;
	private Semaphore mPause = new Semaphore(1);
	private boolean mRemoveOnFail = false;
	private Handler mUiHandler = new Handler();
	private String TAG = "";

	public abstract void task() throws Exception;

	/**
	 * @param completeCallback
	 *            Provide an interface callback to reporting when this task is
	 *            complete.
	 */
	public Task(CompleteCallback completeCallback)
	{
		mCompleteCallback = completeCallback;
	}

	/**
	 * @param tag
	 *            Set the tag of this Task. You can use it to identify the Task
	 *            in the callback.
	 */
	public void setTag(String tag)
	{
		TAG = tag;
	}

	/**
	 * @return the TAG of this Task.
	 */
	public String getTag()
	{
		return TAG;
	}

	/**
	 * @param completeCallback
	 *            Aside from the constructor you can specify the callback using
	 *            this method.
	 */
	public void setCompleteCallback(CompleteCallback completeCallback)
	{
		mCompleteCallback = completeCallback;
	}

	/**
	 * @param taskExecutor
	 *            If you want this Task to automatically retrieve itself from
	 *            the TaskExecutor's queue, a reference is needed.
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor)
	{
		mTaskExecutor = taskExecutor;
	}

	/**
	 * @param removeOnFail
	 *            If the task fails to execute because of an exception, do you
	 *            still want to remove it from the queue?
	 */
	public void setRemoveOnFail(Boolean removeOnFail)
	{
		mRemoveOnFail = removeOnFail;
	}

	/**
	 * 
	 */
	public void pause()
	{
		mPause.tryAcquire();
	}

	/**
	 * 
	 */
	public void resume()
	{
		mPause.release();
	}

	@Override
	public void run()
	{
		try
		{
			mPause.acquire();
			task();
			if (mTaskExecutor != null)
				mTaskExecutor.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskCompletion(Task.this, true, null);
				}
			});
		} catch (final Exception e)
		{
			if (mRemoveOnFail && mTaskExecutor != null)
				mTaskExecutor.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskCompletion(Task.this, false, e);
				}
			});
		}
	}
}