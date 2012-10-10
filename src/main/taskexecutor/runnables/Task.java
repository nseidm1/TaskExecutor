package main.taskexecutor.runnables;
import java.util.concurrent.Semaphore;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import android.os.Bundle;
import android.os.Handler;
/**
 * @author nseidm1
 * 
 */
public class Task implements Runnable
{
    private TaskExecutor mTaskExecutor;
    private TaskCompletedCallback mCompleteCallback;
    private Semaphore mPause = new Semaphore(1);
    private boolean mRemoveOnException = true;
    private boolean mRemoveOnSuccess = true;
    private boolean mExperiencedException = false;
    private Handler mUiHandler;
    private Bundle mBundle = new Bundle();
    private String TAG = "";
    public Task(String tag)
    {
	TAG = tag;
    }
    /**
     * Define the task you want to perform on the supplied bundle.
     * 
     * @throws Exception
     */
    public void task() throws Exception
    {
    }
    /**
     * @param uiHandler
     *            Set the ui handler for this Task.
     */
    public void setUiHandler(Handler uiHandler)
    {
	mUiHandler = uiHandler;
    }
    /**
     * @param bundle
     */
    public void setBundle(Bundle bundle)
    {
	mBundle = bundle;
    }
    /**
     * @return The bundle.
     */
    public Bundle getBundle()
    {
	return mBundle;
    }

    /**
     * @return If an exception occurred.
     */
    public boolean getExperiencedException()
    {
	return mExperiencedException;
    }
    /**
     * @param tag
     *            The TAG needs to be unique as it's used to persist the Task to
     *            disk.
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
     * @param removeOnSuccess
     *            By default Tasks are removed from the queue when they
     *            successfully complete. You can set this to false keeping
     *            successful Tasks in the queue.
     */
    public void setRemoveOnSuccess(Boolean removeOnSuccess)
    {
	mRemoveOnSuccess = removeOnSuccess;
    }
    /**
     * Block this Task before the callback.
     */
    public void pause()
    {
	mPause.drainPermits();
    }
    /**
     * Resume this task unblocking the callback.
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
	    if (mRemoveOnSuccess)
		mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null)
	    {
		mUiHandler.post(new Runnable()
		{
		    @Override
		    public void run()
		    {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, TAG, null);
		    }
		});
	    }
	}
	catch (final Exception e)
	{
	    mExperiencedException = true;
	    if (mRemoveOnException)
		mTaskExecutor.removeTaskFromQueue(this);
	    if (mUiHandler != null)
	    {
		mUiHandler.post(new Runnable()
		{
		    @Override
		    public void run()
		    {
			if (mCompleteCallback != null)
			    mCompleteCallback.onTaskComplete(mBundle, TAG, e);
		    }
		});
	    }
	}
    }
}