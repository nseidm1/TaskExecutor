package com.taskexecutor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.taskexecutor.Helpers.ServiceHelper;
import com.taskexecutor.callbacks.TaskExecutorReferenceCallback;
public class TaskExecutorService extends Service
{
    private TaskExecutor mTaskExecutor = new TaskExecutor();
    private static SoftReference<TaskExecutorReferenceCallback> mSoftCallback;
    public static void requestExecutorReference(Context context, TaskExecutorReferenceCallback serviceReferenceCallback)
    {
	mSoftCallback = new SoftReference<TaskExecutorReferenceCallback>(serviceReferenceCallback);
	context.startService(new Intent(context, TaskExecutorService.class));
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
	mSoftCallback.get().getTaskExecutorReference(mTaskExecutor);
	return Service.START_STICKY;
    }
    @Override
    public void onCreate()
    {
	super.onCreate();
	try
	{
	    ServiceHelper.retrieveTasksFromDisk(this, mTaskExecutor);
	    ServiceHelper.deleteSavedQueue(this);
	    mTaskExecutor.executeQueue();
	}
	catch (FileNotFoundException e)
	{
	    //No queue available to restore
	}
	catch (IOException e)
	{
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
    }
    @Override
    public void onDestroy()
    {
	super.onDestroy();
	try
	{
	    mTaskExecutor.stopExecution(false);
	    ServiceHelper.persistQueueToDisk(this, mTaskExecutor);
	}
	catch (IOException e)
	{
	    Log.e(TaskExecutorService.class.getName(), "Queue could not be saved.");
	}
    }
    @Override
    public IBinder onBind(Intent arg0)
    {
	return null;
    }
}