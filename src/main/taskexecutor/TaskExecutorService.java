package main.taskexecutor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import main.taskexecutor.callbacks.ServiceHelperCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import main.taskexecutor.helpers.QueueOnDiskHelper;
import main.taskexecutor.runnables.QueueToDiskTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import main.taskexecutor.classes.Log;
/**
 * @author nseidm1
 * 
 */
public class TaskExecutorService extends Service implements ServiceHelperCallback
{
    private boolean mHaveTasksBeenRestored = false;
    private TaskExecutor mTaskExecutor = new TaskExecutor(this);
    private QueueToDiskTask mQueueToDisk = new QueueToDiskTask(mTaskExecutor, this);
    private Executor mQueuePersister = Executors.newSingleThreadExecutor();
    private static TaskExecutorReferenceCallback mSoftCallback;
    private static TasksRestoredCallback mTasksRestoredCallback;
    /**
     * @param context
     *            Provide a context to launch the service.
     * @param serviceReferenceCallback
     *            Provide a callback to pass a reference of the TaskExecutor.
     */
    public static void requestExecutorReference(Context context, TaskExecutorReferenceCallback serviceReferenceCallback, TasksRestoredCallback tasksRestoredCallback)
    {
	mSoftCallback = serviceReferenceCallback;
	mTasksRestoredCallback = tasksRestoredCallback;
	context.startService(new Intent(context, TaskExecutorService.class));
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
	if (intent != null)
	{
	    mSoftCallback.getTaskExecutorReference(mTaskExecutor);
	    mSoftCallback = null;
	    if (mHaveTasksBeenRestored)
	    {
		mTasksRestoredCallback.tasksHaveBeenRestored();
		mHaveTasksBeenRestored = false;
	    }
	    mTasksRestoredCallback = null;
	}
	return Service.START_STICKY;
    }
    @Override
    public void onCreate()
    {
	super.onCreate();
	try
	{
	    if (QueueOnDiskHelper.retrieveTasksFromDisk(this, mTaskExecutor))
		mHaveTasksBeenRestored = true;
	}
	catch (FileNotFoundException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (IllegalArgumentException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (InstantiationException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (IllegalAccessException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (InvocationTargetException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (NoSuchMethodException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
	catch (ClassNotFoundException e)
	{
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error retrieving existing queue.");
	}
    }
    @Override
    public void queueModified()
    {
	mQueuePersister.execute(mQueueToDisk);
    }
    // Reserved for IPC, Binder Shminder
    @Override
    public IBinder onBind(Intent arg0)
    {
	return null;
    }
}