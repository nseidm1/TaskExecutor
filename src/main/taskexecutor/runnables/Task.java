package main.taskexecutor.runnables;

import java.util.Random;
import java.util.concurrent.Semaphore;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import android.os.Bundle;
import android.os.Handler;
import main.taskexecutor.classes.Log;

/**
 * @author nseidm1
 */
public abstract class Task implements Runnable {
    private TaskExecutor mTaskExecutor;
    private TaskCompletedCallback mCompleteCallback;
    private Handler mUiHandler;
    private String TAG = "";
    private boolean mShouldRemoveFromQueueOnSuccess = true;
    private boolean mShouldRemoveFromQueueOnException = true;
    private Semaphore mPause = new Semaphore(1);
    private Bundle mBundle = new Bundle();
    private Random mRandom = new Random();

    /**
     * Define the task you want to perform on the supplied bundle.
     * 
     * @throws Exception
     */
    public abstract void task() throws Exception;

    public Task() {
	TAG = Long.toString(mRandom.nextLong());
    }

    /**
     * @param shouldRemoveFromQueueOnSuccess
     *            Default is true, but the Task can remain in the queue on
     *            success if desired. This means it can be re-executed again.
     */
    public void setShouldRemoveFromQueueOnSuccess(
	    boolean shouldRemoveFromQueueOnSuccess) {
	mShouldRemoveFromQueueOnSuccess = shouldRemoveFromQueueOnSuccess;
    }

    /**
     * @param shouldRemoveFromQueueOnException
     *            Default is true, but the Task can remain in the queue on
     *            exception if desired. This means it can be re-executed again.
     */
    public void setShouldRemoveFromQueueOnException(
	    boolean shouldRemoveFromQueueOnException) {
	mShouldRemoveFromQueueOnException = shouldRemoveFromQueueOnException;
    }

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
     *            disk. If your tags duplicate they will overwrite each other on
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
	    if (mShouldRemoveFromQueueOnSuccess)
		mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null) {
		mUiHandler.post(new Runnable() {
		    @Override
		    public void run() {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, null);
			if (mShouldRemoveFromQueueOnSuccess)
			    mCompleteCallback = null;
		    }
		});
	    } else {
		if (mShouldRemoveFromQueueOnSuccess)
		    mCompleteCallback = null;
	    }
	} catch (final Exception e) {
	    Log.d(Task.class.getName(),
		    "Should Remove From Queue on Exception: "
			    + mShouldRemoveFromQueueOnException);
	    if (mShouldRemoveFromQueueOnException)
		mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null) {
		mUiHandler.post(new Runnable() {
		    @Override
		    public void run() {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, e);
			if (mShouldRemoveFromQueueOnException)
			    mCompleteCallback = null;
		    }
		});
	    } else {
		if (mShouldRemoveFromQueueOnException)
		    mCompleteCallback = null;
	    }
	}
    }
}