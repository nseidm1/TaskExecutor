package main.taskexecutor.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import main.taskexecutor.callbacks.ServiceActivityCallback;
import main.taskexecutor.callbacks.ServiceExecutorCallback;
import main.taskexecutor.callbacks.ExecutorReferenceCallback;
import main.taskexecutor.classes.Log;
import main.taskexecutor.helpers.QueueOnDiskHelper;
import main.taskexecutor.helpers.QueueToDiskTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author nseidm1
 */
public class TaskExecutorService extends Service implements ServiceExecutorCallback{
    private              	  boolean                   mHaveTasksBeenRestored     = false;
    private              	  TaskExecutor              mTaskExecutor              = new TaskExecutor(this);
    private              	  Executor                  mQueuePersister            = Executors.newSingleThreadExecutor();
    private              	  QueueToDiskTask           mQueueToDisk               = new QueueToDiskTask(mTaskExecutor, this);
    private volatile static       ServiceActivityCallback   mServiceActivityCallback   = null;
    private volatile static       ExecutorReferenceCallback mExecutorReferenceCallback = null;
    private              	  int                       CURRENT_SERVICE_MODE       = CALLBACK_DEPENDENT;
    public  	     static final int                       CALLBACK_INCONSIDERATE     = 0;
    public  	     static final int                       CALLBACK_DEPENDENT         = 1;
    public  	     static final String                    SERVICE_MODE_KEY           = "SERVICE_MODE_KEY";

    /**
     * @param MODE
     * Provide a mode, either CALLBACK_INCONSIDERATE, or CALLBACK_DEPENDENT.
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
    public static void requestExecutorReference(int                       MODE, 
	    					Context                   context, 
	    					ExecutorReferenceCallback executorReferenceCallback, 
	    					ServiceActivityCallback   serviceActivityCallback) {
	mExecutorReferenceCallback = executorReferenceCallback;
	mServiceActivityCallback   = serviceActivityCallback;
	context.startService(new Intent(context, TaskExecutorService.class).putExtra(SERVICE_MODE_KEY, MODE));
    }

    @Override
    public int onStartCommand(Intent intent, 
	    		      int    flags, 
	    		      int    startId){
	CURRENT_SERVICE_MODE = intent.getIntExtra(SERVICE_MODE_KEY, CALLBACK_DEPENDENT);
	Log.d(TaskExecutorService.class.getName(), "Current Service Mode: " + CURRENT_SERVICE_MODE);
	if (mExecutorReferenceCallback != null)
	    mExecutorReferenceCallback.getTaskExecutorReference(mTaskExecutor);
	if (mHaveTasksBeenRestored){
	    mHaveTasksBeenRestored = false;
	    switch (CURRENT_SERVICE_MODE){
	    case CALLBACK_INCONSIDERATE:
		Log.d(TaskExecutorService.class.getName(), "Tasks Executing, Callback Inconsiderate Mode");
		mTaskExecutor.executeQueue();
		break;
	    case CALLBACK_DEPENDENT:
		if (mServiceActivityCallback != null)
		    mServiceActivityCallback.tasksHaveBeenRestored();
		break;
	    }
	}
	mExecutorReferenceCallback = null;
	mServiceActivityCallback = null;
	return Service.START_REDELIVER_INTENT;
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