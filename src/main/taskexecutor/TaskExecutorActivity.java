package main.taskexecutor;

import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import android.support.v4.app.FragmentActivity;

/**
 * @author nseidm1
 */
public abstract class TaskExecutorActivity extends FragmentActivity implements
	TasksRestoredCallback, TaskCompletedCallback,
	TaskExecutorReferenceCallback {
    protected TaskExecutor mTaskExecutor;

    /**
     * @return Task finess will pause currently running Tasks prior to their
     *         hard callback to accommodate onPause events; this allows for the
     *         hard callback to be reset when the activity is resumed. Be
     *         careful, what if your activity isn't resumed for a long time?
     *         Block your Task's execution wisely.
     */
    public abstract boolean allowTaskFiness();

    /**
     * Provide a mode, either CALLBACK_INCONSIDERATE, or CALLBACK_DEPENDENT.
     * This tells the service how to behave if it's restarted.
     * CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
     * 
     * @return return either TaskExecutorService.CALLBACK_INCONSIDERATE or
     *         TaskExecutorService.CALLBACK_DEPENDENT.
     */
    public abstract int specifyServiceMode();

    @Override
    public void onPause() {
	super.onPause();
	// Theoretically the activity can be finishing before the request for
	// the executor reference is received.
	if (allowTaskFiness() && mTaskExecutor != null)
	    mTaskExecutor.restrainTasks();
    }

    @Override
    public void onResume() {
	super.onResume();
	TaskExecutorService.requestExecutorReference(specifyServiceMode(),
		this, this, this);
    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor) {
	mTaskExecutor = taskExecutor;
	if (allowTaskFiness())
	    mTaskExecutor.finessTasks(this);
    }

    @Override
    public void tasksHaveBeenRestored() {
	mTaskExecutor.finessTasks(this);
	mTaskExecutor.executeQueue();
    }
}