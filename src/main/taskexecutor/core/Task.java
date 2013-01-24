package main.taskexecutor.core;

import android.os.*;
import android.util.*;
import java.util.*;

/**
 * 
 * Welcome to the Task. This is where you get stuff done. There's one abstract method just like a Runnable that you'll implement to get your work done.
 * The Task has the two main callbacks you're accustomed to in AsyncTasks, {@link #postUpdate(Bundle)}, and {@link #postComplete(Boolean, Exception)}.
 * postComplete is automatically managed, but set to protected if you want to define it yourself. By default it will pass the main Task bundle to the
 * TaskExecutor's onTaskComplete method that you end up defining in your Activity's abstract implementation. Updates don't have a default implementation 
 * so you'll want to use postUpdate when you define the {@link #task()} method; postUpdate will execute on the UI thread.
 */
public abstract class Task implements Runnable{
    private TaskExecutor mTaskExecutor               = null;
    private String       TAG                         = "";
    private boolean      mRemoveFromQueueOnSuccess   = true;
    private boolean      mRemoveFromQueueOnException = true;
    private Bundle       mBundle                     = new Bundle();
    private Random       mRandom                     = new Random();

    /**
     * Define your desired behavior if no activity is available when your Task completes execution. Basically this defines how {@link #postComplete(Boolean, Exception)} handles the main Task 
     * bundle.
     * @return Boolean
     */
    public abstract boolean queueResultsIfNoActivity();
    /**
     * Define the task you want to perform.
     * @throws Exception
     */
    public abstract void task() throws Exception;

    public Task(){
	TAG = Long.toString(mRandom.nextLong());
    }

    /**
     * @param removeFromQueueOnSuccess
     * Default is true, but the Task can remain in the queue on success if
     * desired. This means it can be re-executed again.
     */
    public void setRemoveFromQueueOnSuccess(boolean RemoveFromQueueOnSuccess){
	mRemoveFromQueueOnSuccess = RemoveFromQueueOnSuccess;
    }

    public boolean getRemoveFromQueueOnSuccess(){
	return mRemoveFromQueueOnSuccess;
    }

    /**
     * @param removeFromQueueOnException
     * Default is true, but the Task can remain in the queue on exception if
     * desired. This means it can be re-executed again.
     */
    public void setRemoveFromQueueOnException(boolean RemoveFromQueueOnException){
	mRemoveFromQueueOnException = RemoveFromQueueOnException;
    }

    public boolean getRemoveFromQueueOnException(){
	return mRemoveFromQueueOnException;
    }

    /**
     * @param bundle
     */
    public void setMainBundle(Bundle bundle){
	mBundle = bundle;
    }

    /**
     * @return The bundle.
     */
    public Bundle getMainBundle(){
	return mBundle;
    }

    /**
     * @param tag
     * The TAG needs to be unique as it's used to persist the Task to disk. If
     * your tags duplicate they will overwrite each other on disk.
     */
    public void setTag(String tag){
	TAG = tag;
    }

    /**
     * @return the TAG of this Task.
     */
    public String getTag(){
	return TAG;
    }
    
    /**
     * @param taskExecutor
     */
    public void setTaskExecutor(TaskExecutor taskExecutor){
	mTaskExecutor = taskExecutor;
    }

    @Override
    public void run(){
	try{
	    task();
	    postComplete(mRemoveFromQueueOnSuccess, null);
	}catch (final Exception e){
	    postComplete(mRemoveFromQueueOnException, e);
	}
    }
    
    /**
     * Use this method to post updates to the ui thread. Pass strategic
     * bundles that the callback is defined to process. Use this method 
     * strategically when you define the abstract Task method.
     */
    protected void postUpdate(final Bundle bundle){
	mTaskExecutor.mHandler.post(new Runnable(){
	    @Override
	    public void run(){
		if(mTaskExecutor.mTaskUpdateCallback != null){
		    mTaskExecutor.mTaskUpdateCallback.onTaskUpdate(bundle);
		}
	    }
	});
    }

    /**
     * @param shouldRemove
     * @param exception
     * The main bundle of the Task will be passed to the TaskCompletedCallback managed by the TaskExecutor, or it will be queued for delivery
     * if no currently visible Activity is available. You can enable and disable the queueing {@link #queueResultsIfNoActivity()}
     */
    private void postComplete(final boolean   shouldRemove, 
	    		      final Exception exception){
	if (shouldRemove)
	    mTaskExecutor.removeTaskFromQueue(Task.this);
	mTaskExecutor.mHandler.postAtFrontOfQueue(new Runnable(){
	    @Override
	    public void run(){
		if(mTaskExecutor.mTaskCompletedCallback != null){
		    mTaskExecutor.mTaskCompletedCallback.onTaskComplete(mBundle, exception);
		} else if (queueResultsIfNoActivity()){
		    //The results will post when the next Activity resumes.
		    Pair<Bundle, Exception> pair = new Pair<Bundle, Exception>(mBundle, exception);
		    mTaskExecutor.mPendingCompletedTasks.add(pair);
		}
	    };
	});
    }
    
    public static class PersistenceObject implements Parcelable{
	private String  className                   = "";
	private String  TAG                         = "";
	private boolean removeFromQueueOnSuccess    = true;
	private boolean removeFromQueueOnException  = true;
	private Bundle  bundle                      = new Bundle();

	public PersistenceObject() {
	}

	public PersistenceObject(Parcel parcel){
	    className                        = parcel.readString();
	    TAG                              = parcel.readString();
	    removeFromQueueOnSuccess   	     = parcel.readInt() == 1 ? true : false;
	    removeFromQueueOnException       = parcel.readInt() == 1 ? true : false;
	    bundle                           = parcel.readBundle();
	}

	public PersistenceObject(String  className, 
				 Bundle  bundle, 
				 String  TAG,
				 boolean removeFromQueueOnSuccess,
				 boolean removeFromQueueOnException){
	    this.className                  = className;
	    this.TAG                        = TAG;
	    this.removeFromQueueOnSuccess   = removeFromQueueOnSuccess;
	    this.removeFromQueueOnException = removeFromQueueOnException;
	    this.bundle                     = bundle;
	}

	public String getClassName(){
	    return className;
	}

	public Bundle getBundle(){
	    return bundle;
	}

	public String getTag(){
	    return TAG;
	}

	public boolean getRemoveFromQueueOnSuccess(){
	    return removeFromQueueOnSuccess;
	}

	public boolean getRemoveFromQueueOnException(){
	    return removeFromQueueOnException;
	}

	@Override
	public int describeContents(){
	    return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, 
	                          int    flags){
	    dest.writeString(className                         );
	    dest.writeString(TAG                               );
	    dest.writeInt   (removeFromQueueOnSuccess   ? 1 : 0);
	    dest.writeInt   (removeFromQueueOnException ? 1 : 0);
	    dest.writeBundle(bundle                            );
	}

	public static Parcelable.Creator<PersistenceObject> CREATOR = new Parcelable.Creator<PersistenceObject>(){
	    @Override
	    public PersistenceObject createFromParcel(Parcel in){
		return new PersistenceObject(in);
	    }

	    @Override
	    public PersistenceObject[] newArray(int size){
		return new PersistenceObject[size];
	    }
	};
    }
}
