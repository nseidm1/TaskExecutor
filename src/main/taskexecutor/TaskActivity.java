package main.taskexecutor;

import main.taskexecutor.callbacks.ExecutorReferenceCallback;
import main.taskexecutor.callbacks.TasksRestoredCallback;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.core.TaskExecutorService;
import android.support.v4.app.FragmentActivity;
import main.taskexecutor.callbacks.*;

/**
 * Typically configurations are done using direct method calls, the TaskActivity uses abstract definitions to allow for variation to the configurations 
 * depending on conditions. Defining behavior abstractly also forces definition, rather than optionally allowing configuration by method calls.
 * @author Noah Seidman
 * 
 */
public abstract class TaskActivity extends FragmentActivity implements TaskCompletedCallback,
                                                                       TaskUpdateCallback,
                                                                       TasksRestoredCallback,
								       ExecutorReferenceCallback{
    public    static final String       TAG                    = TaskActivity.class.getName();
    protected              TaskExecutor mTaskExecutor          = null;

    /**
     * Return a Service mode, either {@link ExecutorService#CALLBACK_INCONSIDERATE}, or {@link ExecutorService#CALLBACK_DEPENDENT}.
     * This tells the service how to behave if it's restarted.
     * CALLBACK_DEPENDENT will not execute the queue and will wait for an
     * activity for a hard callback to be available. CALLBACK_INCONSIDERATE will
     * execute the queue without a hard callback being available.
     */
    protected abstract int specifyServiceMode();
    
    /**
     * Return an auto execute mode, either {@link ExecutorService#AUTOEXEC_MODE_ENABLED}, or {@link ExecutorService#AUTOEXEC_MODE_DISABLED}. The service 
     * will auto execute queued Tasks every five seconds automatically without needing to manually call 
     * executeTasks().
     */
    protected abstract int specifyAutoexecMode();

    /**
     * @return When the Service is in {@link ExecutorService#CALLBACK_DEPENDENT} mode, and Tasks are
     * restored from a killed Service, should the queue auto execute on the next
     * Activity launch?
     */
    protected abstract boolean autoExecuteRestoredTasks();
    
    /**
     * It's safe to begin using the TaskExecutor here. The TaskExecutor reference is now available. This method will be called after every onResume.
     */
    protected abstract void taskExecutorReferenceAvailable();
    
    /**
     * @return the current availability of the TaskExecutor reference. 
     */
    public final boolean isTaskExecutorAvailable(){
	return mTaskExecutor != null;
    }

    @Override
    public void onPause(){
	super.onPause();
	if (mTaskExecutor != null)
	    mTaskExecutor.clean();
    }

    @Override
    public void onResume(){
	super.onResume();
	TaskExecutorService.requestTaskExecutorReference(specifyServiceMode(), specifyAutoexecMode(), this, this, this);
    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	mTaskExecutor = taskExecutor;
	taskExecutorReferenceAvailable();
	mTaskExecutor.dirty(this, this);
    }

    @Override
    public void notifyTasksHaveBeenRestored(){
        if(autoExecuteRestoredTasks()){
	    mTaskExecutor.executeQueue();
	}
    }
}
