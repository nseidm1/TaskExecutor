package main.taskexecutor.core;

import android.os.*;
import android.util.*;
import java.util.*;

/**
 * @author Noah Seidman
 */
public abstract class Task implements Runnable{
    private TaskExecutor          mTaskExecutor                     = null;
    private String                TAG                               = "";
    private boolean               mShouldRemoveFromQueueOnSuccess   = true;
    private boolean               mShouldRemoveFromQueueOnException = true;
    private Bundle                mBundle                           = new Bundle();
    private Random                mRandom                           = new Random();

    /**
     * Define the task you want to perform.
     * @throws Exception
     */
    public abstract void task() throws Exception;

    public Task(){
	TAG = Long.toString(mRandom.nextLong());
    }

    /**
     * @param shouldRemoveFromQueueOnSuccess
     * Default is true, but the Task can remain in the queue on success if
     * desired. This means it can be re-executed again.
     */
    public void setShouldRemoveFromQueueOnSuccess(boolean shouldRemoveFromQueueOnSuccess){
	mShouldRemoveFromQueueOnSuccess = shouldRemoveFromQueueOnSuccess;
    }

    public boolean getShouldRemoveFromQueueOnSuccess(){
	return mShouldRemoveFromQueueOnSuccess;
    }

    /**
     * @param shouldRemoveFromQueueOnException
     * Default is true, but the Task can remain in the queue on exception if
     * desired. This means it can be re-executed again.
     */
    public void setShouldRemoveFromQueueOnException(boolean shouldRemoveFromQueueOnException){
	mShouldRemoveFromQueueOnException = shouldRemoveFromQueueOnException;
    }

    public boolean getShouldRemoveFromQueueOnException(){
	return mShouldRemoveFromQueueOnException;
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
	    postComplete(null, mShouldRemoveFromQueueOnSuccess);
	}catch (final Exception e){
	    postComplete(e, mShouldRemoveFromQueueOnException);
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

    private void postComplete(final Exception exception, 
                              final boolean   shouldRemove){
	if (shouldRemove)
	    mTaskExecutor.removeTaskFromQueue(this);
	mTaskExecutor.mHandler.postAtFrontOfQueue(new Runnable(){
	    @Override
	    public void run(){
		if(mTaskExecutor.mTaskCompletedCallback != null){
		    mTaskExecutor.mTaskCompletedCallback.onTaskComplete(mBundle, exception);
		} else{
		    //The results will post when the next Activity resumes.
		    Pair<Bundle, Exception> pair = new Pair<Bundle, Exception>(mBundle, exception);
		    mTaskExecutor.mPendingCompletedTasks.add(pair);
		}
	    };
	});
    }

    public static class PersistenceObject implements Parcelable{
	private String  className                         = "";
	private String  TAG                               = "";
	private boolean shouldRemoveFromQueueOnSuccess    = true;
	private boolean shouldRemoveFromQueueOnException  = true;
	private Bundle  bundle                            = new Bundle();

	public PersistenceObject() {
	}

	public PersistenceObject(Parcel parcel){
	    className                        = parcel.readString();
	    TAG                              = parcel.readString();
	    shouldRemoveFromQueueOnSuccess   = parcel.readInt() == 1 ? true : false;
	    shouldRemoveFromQueueOnException = parcel.readInt() == 1 ? true : false;
	    bundle                           = parcel.readBundle();
	}

	public PersistenceObject(String  className, 
				 Bundle  bundle, 
				 String  TAG,
				 boolean shouldRemoveFromQueueOnSuccess,
				 boolean shouldRemoveFromQueueOnException){
	    this.className                        = className;
	    this.TAG                              = TAG;
	    this.shouldRemoveFromQueueOnSuccess   = shouldRemoveFromQueueOnSuccess;
	    this.shouldRemoveFromQueueOnException = shouldRemoveFromQueueOnException;
	    this.bundle                           = bundle;
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

	public boolean getShouldRemoveFromQueueOnSuccess(){
	    return shouldRemoveFromQueueOnSuccess;
	}

	public boolean getShouldRemoveFromQueueOnException(){
	    return shouldRemoveFromQueueOnException;
	}

	@Override
	public int describeContents(){
	    return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, 
	                          int    flags){
	    dest.writeString(className                               );
	    dest.writeString(TAG                                     );
	    dest.writeInt   (shouldRemoveFromQueueOnSuccess   ? 1 : 0);
	    dest.writeInt   (shouldRemoveFromQueueOnException ? 1 : 0);
	    dest.writeBundle(bundle                                  );
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
