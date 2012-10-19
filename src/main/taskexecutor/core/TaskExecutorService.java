package main.taskexecutor.core;

import android.app.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import main.taskexecutor.callbacks.*;
import main.taskexecutor.classes.*;
import main.taskexecutor.helpers.*;

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
    public  	     static final int                       SERVICE_MODE_CALLBACK_INCONSIDERATE = 0;
    public  	     static final int                       SERVICE_MODE_CALLBACK_DEPENDENT     = 1;
    public 	     static final int                       RETAIN_CURRENT_SERVICE_MODE         = 2;
    private          static    	  int                       CURRENT_SERVICE_MODE                = SERVICE_MODE_CALLBACK_DEPENDENT;
    public           static final int                       AUTOEXEC_MODE_DISABLED              = 0;
    public           static final int                       AUTOEXEC_MODE_ENABLED               = 1;
    public           static final int                       RETAIN_CURRENT_AUTOEXEC_MODE        = 2;
    private          static       int                       CURRENT_AUTOEXEC_MODE               = AUTOEXEC_MODE_DISABLED;
    public  	     static final String                    SERVICE_MODE_KEY                    = "SERVICE_MODE_KEY";
    public           static final String                    AUTOEXEC_MODE_KEY                   = "AUTO_EXECUTE_MODE_KEY";
  
    /**
     * @param MODE
     * Provide a mode, either CALLBACK_INCONSIDERATE, CALLBACK_DEPENDENT, or RETAIN_CURRENT_MODE.
     * This tells the service how to behave if it's restarted.
     * CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
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
	mTasksRestoredCallback   = tasksRestoredCallback;
	if (SERVICE_MODE == RETAIN_CURRENT_SERVICE_MODE)	   
	    SERVICE_MODE = CURRENT_SERVICE_MODE;
	context.startService(new Intent(context, TaskExecutorService.class).putExtra(SERVICE_MODE_KEY, SERVICE_MODE).putExtra(AUTOEXEC_MODE_KEY, AUTOEXEC_MODE));
    }

    @Override
    public int onStartCommand(Intent intent, 
	    		      int    flags, 
	    		      int    startId){
	CURRENT_SERVICE_MODE = intent.getIntExtra(SERVICE_MODE_KEY, SERVICE_MODE_CALLBACK_DEPENDENT);
	CURRENT_AUTOEXEC_MODE = intent.getIntExtra(AUTOEXEC_MODE_KEY, AUTOEXEC_MODE_DISABLED);
	if(CURRENT_AUTOEXEC_MODE == AUTOEXEC_MODE_ENABLED){
	    startAutoExec();
	} else{
	    stopAutoExec();
	}
	Log.d(TaskExecutorService.class.getName(), "Current Service Mode: " + CURRENT_SERVICE_MODE);
	if (mExecutorReferenceCallback != null)
	    mExecutorReferenceCallback.getTaskExecutorReference(mTaskExecutor);
	if (mHaveTasksBeenRestored){
	    switch (CURRENT_SERVICE_MODE){
	    case SERVICE_MODE_CALLBACK_INCONSIDERATE:
		Log.d(TaskExecutorService.class.getName(), "Tasks Executing, Callback Inconsiderate Mode");
		mTaskExecutor.executeQueue();
		mHaveTasksBeenRestored = false;
		break;
	    case SERVICE_MODE_CALLBACK_DEPENDENT:
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
    
    private void startAutoExec(){
        mHandler.post(autoexecTask);
    }
    
    private Runnable autoexecTask = new Runnable(){
	@Override
	public void run(){
	    mTaskExecutor.executeQueue();
	    mHandler.postDelayed(this, 5000);
	}
    };
    
    private void stopAutoExec(){
	mHandler.removeCallbacks(autoexecTask);
    }

    @Override
    public void onCreate(){
	super.onCreate();
	try {
	    if (QueueOnDiskHelper.retrieveTasksFromDisk(this, mTaskExecutor))
		mHaveTasksBeenRestored = true;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (IOException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (InstantiationException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	} catch (ClassNotFoundException e) {
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
