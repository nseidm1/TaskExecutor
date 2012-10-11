package main.taskexecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import android.support.v4.app.FragmentActivity;
/**
 * @author nseidm1
 * 
 */
public abstract class TaskExecutorActivity extends FragmentActivity implements TasksRestoredCallback, TaskCompletedCallback, TaskExecutorReferenceCallback
{
    protected TaskExecutor mTaskExecutor;
    /**
     * @return Task finess will pause currently running Tasks prior to their
     *         hard callback, allowing for it to be reset when the activity is
     *         resumed. Be careful, what if your activity isn't resumed for a
     *         long time? Tasks that are restored from a crashed Service will
     *         always be finessed.
     */
    public abstract boolean allowTaskFiness();
    public abstract int specifyServiceMode();
    @Override
    public void onPause()
    {
	super.onPause();
	if (allowTaskFiness())
	    mTaskExecutor.restrainTasks();
    }
    @Override
    public void onResume()
    {
	super.onResume();
	TaskExecutorService.requestExecutorReference(specifyServiceMode(), this, this, this);
    }
    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor)
    {
	mTaskExecutor = taskExecutor;
	if (allowTaskFiness())
	    mTaskExecutor.finessTasks(this);
    }
    @Override
    public void tasksHaveBeenRestored()
    {
	mTaskExecutor.finessTasks(this);
	mTaskExecutor.executeQueue();
    }
}