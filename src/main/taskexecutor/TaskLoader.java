package main.taskexecutor;

import java.util.concurrent.Future;

import main.taskexecutor.core.Task;
import main.taskexecutor.core.TaskExecutor;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.Loader;

/**
 * @author Noah Seidman
 */
public abstract class TaskLoader<D> extends Loader<D> {

    public static final String TAG = TaskLoader.class.getName();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TaskExecutor mTaskExecutor = null;
    private Future<?> mFuture = null;
    private D mData = null;

    /**
     * Just like Tasks, define your asynchronous code needed to generate the
     * data you want the Loader to manage. Return the data from the method you
     * define.
     * 
     * @return The specified data type defined in the subclass parameterization.
     * @throws Exception
     *             Throw exception so the deliverResult callback always gets hit
     *             even with null.
     */
    protected abstract D loaderTask() throws Exception;

    public TaskLoader(TaskActivity activity) {
	super(activity);
	mTaskExecutor = activity.mTaskExecutor;
    }

    /**
     * Were not going to use the abstract task method. We override the run
     * method so the Task doesn't call the managed ui callbacks; there's
     * reasoning to this that follows. We will not be using the Task's callbacks
     * that are managed by the TaskExecutor. We want to use the Task and the
     * TaskExecutor to consolidate all asynchronous code execution, but we want
     * the ui post to be here.
     */
    private final class LoaderTask extends Task {
	private D result = null;

	@Override
	public void task() throws Exception {
	}

	@Override
	public void run() {
	    try {
		result = loaderTask();
	    } catch (Exception e) {
		// Print the stack trace, and allow things to continue. The
		// Loader will return null and the dev will see the exception in
		// the LogCat.
		e.printStackTrace();
	    }
	    mData = result;
	    mHandler.post(new Runnable() {
		@Override
		public void run() {
		    deliverResult(result);
		}
	    });
	}

	@Override
	public boolean queueResultsIfNoActivity() {
	    return false;
	}
    }

    @Override
    protected void onStartLoading() {
	if (mData != null) {
	    deliverResult(mData);
	} else {
	    forceLoad();
	}
    }

    @Override
    protected void onForceLoad() {
	mFuture = mTaskExecutor.executeTask(new LoaderTask());
    }

    @Override
    protected void onReset() {
	onStopLoading();
	mData = null;
    }

    @Override
    protected void onStopLoading() {
	if (mFuture != null)
	    mFuture.cancel(true);
    }
}
