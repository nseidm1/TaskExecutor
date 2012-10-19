package main.taskexecutor;

import main.taskexecutor.callbacks.ExecutorReferenceCallback;
import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.core.TaskExecutorService;
import android.support.v4.app.DialogFragment;

/**
 * The TaskExecutorFragment will request a reference to the TaskExecutor, but is not designed to get the callback for any 
 * executed Tasks. Please reference the calling TaskExecutorActivity for the callback.
 * @author Noah Seidman
 */
public abstract class TaskExecutorFragment extends DialogFragment implements ExecutorReferenceCallback{

    protected TaskExecutor mTaskExecutor;

    @Override
    public void onResume(){
	super.onResume();
	TaskExecutorService.requestExecutorReference(TaskExecutorService.RETAIN_CURRENT_SERVICE_MODE, TaskExecutorService.RETAIN_CURRENT_AUTOEXEC_MODE, getActivity(), this, null);
    }
    
    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	mTaskExecutor = taskExecutor;
    }
}
