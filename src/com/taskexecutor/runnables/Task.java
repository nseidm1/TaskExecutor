package com.taskexecutor.runnables;

import java.util.concurrent.Semaphore;

import android.os.Handler;

import com.taskexecutor.TaskExecutor;
import com.taskexecutor.callbacks.TaskCompletedCallback;

public abstract class Task implements Runnable
{
	private TaskExecutor mTaskExecutor;
	private TaskCompletedCallback mCompleteCallback;
	private Semaphore mPause = new Semaphore(1);
	private boolean mRemoveOnException = false;
	private boolean mExperiencedException  = false;
	private Handler mUiHandler = new Handler();
	private String TAG = "";

	public abstract void task() throws Exception;

	/**
	 * @param completeCallback
	 *            Provide an interface callback to reporting when this task is
	 *            complete.
	 */
	public Task(TaskCompletedCallback completeCallback)
	{
		mCompleteCallback = completeCallback;
	}
	
	/**
	 * @return If an exception occured.
	 */
	public boolean getExperiencedException()
	{
		return mExperiencedException;
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
	public void setCompleteCallback(TaskCompletedCallback completeCallback)
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
	public void setRemoveOnException(Boolean removeOnException)
	{
		mRemoveOnException = removeOnException;
	}

	/**
	 * 
	 */
	public void pause()
	{
		mPause.drainPermits();
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
			task();
			mPause.acquire();
			if (mTaskExecutor != null)
				mTaskExecutor.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskComplete(Task.this, true, null);
				}
			});
		} catch (final Exception e)
		{
			mExperiencedException = true;
			if (mRemoveOnException && mTaskExecutor != null)
				mTaskExecutor.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskComplete(Task.this, false, e);
				}
			});
		}
	}
}