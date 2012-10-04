package com.taskexecutorservice.runnables;

import java.util.concurrent.Semaphore;

import com.taskexecutorservice.TaskExecutorService;
import com.taskexecutorservice.callbacks.CompleteCallback;

public abstract class Task implements Runnable {
	private TaskExecutorService mTaskService;
	private CompleteCallback mCompleteCallback;
	private Semaphore mPause = new Semaphore(1);
	private boolean mRemoveOnFail = false;

	public abstract void defineTask() throws Exception;

	public Task(TaskExecutorService taskService, CompleteCallback completeCallback) {
		mTaskService = taskService;
		mCompleteCallback = completeCallback;
	}

	public void setCompleteCallback(CompleteCallback completeCallback) {
		mCompleteCallback = completeCallback;
	}

	public void setRemoveOnFail(Boolean removeOnFail) {
		mRemoveOnFail = removeOnFail;
	}

	public void pause() {
		mPause.tryAcquire();
	}

	public void resume() {
		mPause.release();
	}

	@Override
	public void run() {
		try {
			mPause.acquire();
			defineTask();
			mTaskService.removeTaskFromQueue(this);
			mCompleteCallback.onTaskCompletion(this, true, null);
		} catch (Exception e) {
			if (mRemoveOnFail)
				mTaskService.removeTaskFromQueue(this);
			if (mCompleteCallback != null)
				mCompleteCallback.onTaskCompletion(this, false, e);
		}
	}
}