package com.taskexecutorservice.runnables;

import java.util.concurrent.Semaphore;

import android.os.Handler;

import com.taskexecutorservice.TaskExecutorService;
import com.taskexecutorservice.callbacks.CompleteCallback;

public abstract class Task implements Runnable {
	private TaskExecutorService mTaskExecutorService;
	private CompleteCallback mCompleteCallback;
	private Semaphore mPause = new Semaphore(1);
	private boolean mRemoveOnFail = false;
	private Handler mUiHandler;
	public abstract void defineTask() throws Exception;

	/**
	 * @param taskService
	 * Provide a reference to the TaskExecutorService so when this task is complete it can remove itself from an existing queue.
	 * @param completeCallback
	 * Provide an interface callback to reporting when this task is complete.
	 * @param uiHandler
	 */
	public Task(TaskExecutorService taskExecutorService, CompleteCallback completeCallback, Handler uiHandler) {
		mTaskExecutorService = taskExecutorService;
		mCompleteCallback = completeCallback;
		if (uiHandler == null)
		{
			mUiHandler = new Handler();
		}
		else 
		{
			mUiHandler = uiHandler;
		}
	}

	/**
	 * @param completeCallback
	 * Aside from the constructor you can specify the callback using this method.
	 */
	public void setCompleteCallback(CompleteCallback completeCallback) {
		mCompleteCallback = completeCallback;
	}

	/**
	 * @param removeOnFail
	 * If the task fails to execute because of an exception, do you still want to remove it from the queue?
	 */
	public void setRemoveOnFail(Boolean removeOnFail) {
		mRemoveOnFail = removeOnFail;
	}

	/**
	 * 
	 */
	public void pause() {
		mPause.tryAcquire();
	}

	/**
	 * 
	 */
	public void resume() {
		mPause.release();
	}

	@Override
	public void run() {
		try {
			mPause.acquire();
			defineTask();
			if (mTaskExecutorService != null)
				mTaskExecutorService.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskCompletion(Task.this, true, null);
				}
			});
		} catch (final Exception e) {
			if (mRemoveOnFail && mTaskExecutorService != null)
				mTaskExecutorService.removeTaskFromQueue(this);
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mCompleteCallback != null)
						mCompleteCallback.onTaskCompletion(Task.this, false, e);
				}
			});
		}
	}
}