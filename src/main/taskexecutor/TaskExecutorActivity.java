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
     * Provide a mode, either CALLBACK_INCONSIDERATE, or CALLBACK_DEPENDENT.
     * This tells the service how to behave if it's restarted.
     * CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
     * 
     * @return return either TaskExecutorService.CALLBACK_INCONSIDERATE or
     * TaskExecutorService.CALLBACK_DEPENDENT.
     */
    protected abstract int specifyServiceMode();

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
	TaskExecutorService.requestExecutorReference(specifyServiceMode(), this, this, this);
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
