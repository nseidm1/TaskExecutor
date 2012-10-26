
package main.taskexecutor;

import java.util.concurrent.Future;

import main.taskexecutor.core.Task;
import main.taskexecutor.core.TaskExecutor;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.Loader;

/**
 * @author Noah Seidman
 */
public abstract class TaskLoader<D> extends Loader<D>{
    
    private              Handler      mHandler      = new Handler(Looper.getMainLooper());
    public  static final String       TAG           = TaskLoader.class.getName();
    private              TaskExecutor mTaskExecutor = null;
    protected            D            mData         = null;
    private              Future<?>    mFuture       = null;

    /**
     * Just like Tasks, define your asynchronous code needed to generate the data you want 
     * the Loader to manage. Return the data from the method you define.
     * @return
     * The specified type defined in the subclass parameterization.
     * @throws Exception
     * Throw exception so the deliverResult callback always gets hit even with null.
     */
    protected abstract D loaderTask() throws Exception;
    
    public TaskLoader(Context context, TaskExecutor taskExecutor){
        super(context);
	mTaskExecutor = taskExecutor;
    }
    
    /**
     * 	Were not going to use the abstract task method.
     *  We override the run method so the Task doesn't call the internal post() method; there's 
     *  reasoning to this that follows. We will not be using the Task's callback that is managed 
     *  by the TaskExecutor. We want to use the Task and the TaskExecutor to consolidate all 
     *  asynchronous Task execution, but we want the ui post to be here.
     * @author nseidman
     *
     */
    private final class LoaderTask extends Task{
	private D result = null;
	@Override
	public void task() throws Exception{}
	@Override
	public void run(){
	    try{
		result = loaderTask();
	    }catch (Exception e){
		e.printStackTrace();
	    }
	    mData = result;
	    mHandler.post(new Runnable(){
		@Override
		public void run(){
		    deliverResult(result);
		}
	    });	
	}
    }
    
    @Override
    protected void onStartLoading(){
	if (mData != null) {
	    deliverResult(mData);
	}
	else{
	    forceLoad();
	}
    }
    
    @Override
    protected void onForceLoad(){
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