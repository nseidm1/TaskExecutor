package main.taskexecutor;

import main.taskexecutor.callbacks.ExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.core.TaskExecutorService;
import android.support.v4.app.FragmentActivity;

/**
 * @author Noah Seidman
 */
public abstract class TaskExecutorActivity extends FragmentActivity implements TaskCompletedCallback, 
									       TasksRestoredCallback,
									       ExecutorReferenceCallback{
    protected TaskExecutor mTaskExecutor;

    /**
     * @return Task finess will pause currently running Tasks prior to their
     * hard callback to accommodate onPause events; this allows for the hard
     * callback to be reset when the activity is resumed, or a new one is 
     * being created. Be careful, what if your activity isn't resumed for a 
     * long time? Block your Task's execution wisely. Please consider using 
     * setInterruptTaskAfter(int milliseconds) in the TaskExecutor if you 
     * set this to true.
     */
    protected abstract boolean allowTaskFiness();

    /**
     * Provide a Service mode, either SERVICE_MODE_CALLBACK_INCONSIDERATE, or SERVICE_MODE_CALLBACK_DEPENDENT.
     * This tells the service how to behave if it's restarted.
     * SERVICE_MODE_CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. SERVICE_MODE_CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
     * 
     * @return return either TaskExecutorService.SERVICE_MODE_CALLBACK_INCONSIDERATE or
     * TaskExecutorService.SERVICE_MODE_CALLBACK_DEPENDENT.
     */
    protected abstract int specifyServiceMode();
    
    /**
     * Provide an auto execuge mode, either AUTOEXEC_MODE_ENABLED, or AUTOEXEC_MODE_DISABLED. The service 
     * will auto execute queued Tasks every five seconds automatically without needing to manually call 
     * executeTasks().
     */
    protected abstract int specifyAutoexecMode();

    /**
     * @return When the Service is in CALLBACK_DEPENDENT mode, and Tasks are
     * restored from a killed Service, should the queue auto execute on the next
     * Activity launch?
     */
    protected abstract boolean autoExecuteRestoredTasks();

    @Override
    public void onPause(){
	super.onPause();
	// Theoretically the activity can be finishing before the request for
	// the executor reference is received.
	if (mTaskExecutor != null)
	    mTaskExecutor.restrain(allowTaskFiness());
    }

    @Override
    public void onResume(){
	super.onResume();
	TaskExecutorService.requestExecutorReference(specifyServiceMode(), specifyAutoexecMode(), this, this, this);
    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	mTaskExecutor = taskExecutor;
	mTaskExecutor.finess(this);
    }

    @Override
    public void notifyTasksHaveBeenRestored(){
	mTaskExecutor.finess(this);
	if (autoExecuteRestoredTasks())
	    mTaskExecutor.executeQueue();
    }
}
