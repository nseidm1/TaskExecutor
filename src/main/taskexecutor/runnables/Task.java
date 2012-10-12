package main.taskexecutor.runnables;

import java.util.Random;
import java.util.concurrent.Semaphore;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author nseidm1
 */
public abstract class Task implements Runnable {
    private TaskExecutor mTaskExecutor;
    private TaskCompletedCallback mCompleteCallback;
    private Handler mUiHandler;
    private String TAG = "";
    private Semaphore mPause = new Semaphore(1);
    private Bundle mBundle = new Bundle();
    private Random mRandom = new Random();

    public Task() {
	TAG = Long.toString(mRandom.nextLong());
    }

    /**
     * Define the task you want to perform on the supplied bundle.
     * 
     * @throws Exception
     */
    public abstract void task() throws Exception;

    /**
     * @param uiHandler
     *            Set the ui handler for this Task.
     */
    public void setUiHandler(Handler uiHandler) {
	mUiHandler = uiHandler;
    }

    /**
     * @param bundle
     */
    public void setBundle(Bundle bundle) {
	mBundle = bundle;
    }

    /**
     * @return The bundle.
     */
    public Bundle getBundle() {
	return mBundle;
    }

    /**
     * @param tag
     *            The TAG needs to be unique as it's used to persist the Task to
     *            disk.
     */
    public void setTag(String tag) {
	TAG = tag;
    }

    /**
     * @return the TAG of this Task.
     */
    public String getTag() {
	return TAG;
    }

    /**
     * @param completeCallback
     *            Aside from the constructor you can specify the callback using
     *            this method.
     */
    public void setCompleteCallback(TaskCompletedCallback completeCallback) {
	mCompleteCallback = completeCallback;
    }

    /**
     * @param taskExecutor
     *            If you want this Task to automatically retrieve itself from
     *            the TaskExecutor's queue, a reference is needed.
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
	mTaskExecutor = taskExecutor;
    }

    /**
     * Block this Task before the callback.
     */
    public void pause() {
	mPause.drainPermits();
    }

    /**
     * Resume this task unblocking the callback.
     */
    public void resume() {
	mPause.release();
    }

    @Override
    public void run() {
	try {
	    task();
	    mPause.acquire();
	    mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null) {
		mUiHandler.post(new Runnable() {
		    @Override
		    public void run() {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, null);
			mCompleteCallback = null;
		    }
		});
	    }
	} catch (final Exception e) {
	    mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null) {
		mUiHandler.post(new Runnable() {
		    @Override
		    public void run() {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, e);
			mCompleteCallback = null;
		    }
		});
	    }
	}
    }
}