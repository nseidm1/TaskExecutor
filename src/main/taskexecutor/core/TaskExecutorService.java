package main.taskexecutor.core;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import main.taskexecutor.callbacks.ExecutorReferenceCallback;
import main.taskexecutor.callbacks.ServiceExecutorCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import main.taskexecutor.classes.Log;
import main.taskexecutor.helpers.QueueOnDiskHelper;
import main.taskexecutor.helpers.QueueToDiskTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * @author Noah Seidman
 */
public class TaskExecutorService extends Service implements ServiceExecutorCallback{
    private                       Handler                   mHandler                            = new Handler(Looper.getMainLooper());
    private              	  boolean                   mHaveTasksBeenRestored              = false;
    private              	  TaskExecutor              mTaskExecutor                       = new TaskExecutor(this);
    private              	  Executor                  mQueuePersister                     = Executors.newSingleThreadExecutor();
    private              	  QueueToDiskTask           mQueueToDisk                        = new QueueToDiskTask(mTaskExecutor, this);
    private volatile static       TasksRestoredCallback     mTasksRestoredCallback              = null;
    private volatile static       ExecutorReferenceCallback mExecutorReferenceCallback          = null;
    public  	     static final int                       CALLBACK_INCONSIDERATE              = 0;
    public  	     static final int                       CALLBACK_DEPENDENT                  = 1;
    private volatile static    	  int                       CURRENT_SERVICE_MODE                = CALLBACK_DEPENDENT;
    public           static final int                       AUTOEXEC_MODE_DISABLED              = 0;
    public           static final int                       AUTOEXEC_MODE_ENABLED               = 1;
    private volatile static       int                       CURRENT_AUTOEXEC_MODE               = AUTOEXEC_MODE_DISABLED;
    public  	     static final String                    SERVICE_MODE_KEY                    = "SERVICE_MODE_KEY";
    public           static final String                    AUTOEXEC_MODE_KEY                   = "AUTO_EXECUTE_MODE_KEY";
  
    /**
     * @param SERVICE_MODE
     * Provide a Service mode, either CALLBACK_INCONSIDERATE, or CALLBACK_DEPENDENT.
     * This tells the service how to behave if it's restarted.
     * CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
     * @param AUTOEXEC_MODE
     * Provide an auto execute mode, either AUTOEXEC_MODE_ENABLED, or AUTOEXEC_MODE_DISABLED. The service 
     * will auto execute queued Tasks every five seconds automatically without needing to manually call 
     * executeTasks().
     * @param context
     * @param ExecutorReferenceCallback
     * The interface that returns a reference to the TaskExecutor.
     * @param tasksRestoredCallback
     * The interface informing an activity if Tasks have been restored by the
     * service after a restart. This interface is ONLY called when the service 
     * is in CALLBACK_DEPENDENT mode.
     */
    public static void requestExecutorReference(int                       SERVICE_MODE, 
                                                int                       AUTOEXEC_MODE,
	    					Context                   context, 
	    					ExecutorReferenceCallback executorReferenceCallback, 
	    					TasksRestoredCallback     tasksRestoredCallback) {
	mExecutorReferenceCallback = executorReferenceCallback;
	mTasksRestoredCallback     = tasksRestoredCallback;
	context.startService(new Intent(context, TaskExecutorService.class).putExtra(SERVICE_MODE_KEY , SERVICE_MODE ).
	                                                                    putExtra(AUTOEXEC_MODE_KEY, AUTOEXEC_MODE));
    }

    @Override
    public int onStartCommand(Intent intent, 
	    		      int    flags, 
	    		      int    startId){
	CURRENT_SERVICE_MODE  = intent.getIntExtra(SERVICE_MODE_KEY , CALLBACK_DEPENDENT);
	CURRENT_AUTOEXEC_MODE = intent.getIntExtra(AUTOEXEC_MODE_KEY, AUTOEXEC_MODE_DISABLED         );
	
	processAutoExec();
	Log.d(TaskExecutorService.class.getName(), "Current Service Mode: " + CURRENT_SERVICE_MODE);
	if (mExecutorReferenceCallback != null)
	    mExecutorReferenceCallback.getTaskExecutorReference(mTaskExecutor);
	if (mHaveTasksBeenRestored){
	    switch (CURRENT_SERVICE_MODE){
	    case CALLBACK_INCONSIDERATE:
		Log.d(TaskExecutorService.class.getName(), "Tasks Executing, Callback Inconsiderate Mode");
		mTaskExecutor.executeQueue();
		mHaveTasksBeenRestored = false;
		break;
	    case CALLBACK_DEPENDENT:
		if (mTasksRestoredCallback != null){
		    mTasksRestoredCallback.notifyTasksHaveBeenRestored();
		    mHaveTasksBeenRestored = false;  
		}
		break;
	    }
	}
	mExecutorReferenceCallback = null;
	mTasksRestoredCallback = null;
	return Service.START_REDELIVER_INTENT;
    }
    
    private void processAutoExec(){
	mHandler.removeCallbacks(autoexecTask);
	if(CURRENT_AUTOEXEC_MODE == AUTOEXEC_MODE_ENABLED)
	    mHandler.post(autoexecTask);
    }
    
    private Runnable autoexecTask = new Runnable(){
	@Override
	public void run(){
	    mTaskExecutor.executeQueue();
	    mHandler.postDelayed(this, 5000);
	}
    };
    
    @Override
    public void onCreate(){
	super.onCreate();
	try {
	    if (QueueOnDiskHelper.retrieveTasksFromDisk(this, mTaskExecutor))
		mHaveTasksBeenRestored = true;
	} catch (Exception e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} 
    }

    @Override
    public void queueModified(){
	mQueuePersister.execute(mQueueToDisk);
    }

    // Reserved for IPC, Binder Shminder
    @Override
    public IBinder onBind(Intent arg0){
	return null;
    }
}
