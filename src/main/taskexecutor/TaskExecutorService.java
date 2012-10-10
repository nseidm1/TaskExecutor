package main.taskexecutor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import main.taskexecutor.callbacks.ServiceHelperCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.helpers.QueueOnDiskHelper;
import main.taskexecutor.runnables.QueueToDiskTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
/**
 * @author nseidm1
 * 
 */
public class TaskExecutorService extends Service implements ServiceHelperCallback
{
    private TaskExecutor mTaskExecutor = new TaskExecutor(this);
    private QueueToDiskTask mQueueToDisk = new QueueToDiskTask(mTaskExecutor, this);
    private Executor mQueuePersister = Executors.newSingleThreadExecutor();
    private static SoftReference<TaskExecutorReferenceCallback> mSoftCallback;
    /**
     * @param context
     *            Provide a context to launch the service.
     * @param serviceReferenceCallback
     *            Provide a callback to pass a reference of the TaskExecutor.
     */
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
	    QueueOnDiskHelper.retrieveTasksFromDisk(this, mTaskExecutor);
	    mTaskExecutor.executeQueue();// If Tasks are restored from disk
					 // begin execution immediately
	}
	catch (FileNotFoundException e)
	{
	    // No queue available to restore
	}
	catch (IOException e)
	{
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