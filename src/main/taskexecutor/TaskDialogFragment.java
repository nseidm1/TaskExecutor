package main.taskexecutor;

import android.os.*;
import android.support.v4.app.*;
import main.taskexecutor.callbacks.*;
import main.taskexecutor.core.*;

/**
 * The TaskDialogFragment will request a reference to the TaskExecutor, but is not designed to get the callback for any 
 * executed Tasks. Please reference the calling TaskActivity for the callback.
 * @author Noah Seidman
 */
public abstract class TaskDialogFragment extends DialogFragment implements ExecutorReferenceCallback{

    public static final String       TAG           = TaskDialogFragment.class.getName();
    protected           TaskExecutor mTaskExecutor = null;

    @Override
    public void onActivityCreated(Bundle bundle){
	super.onActivityCreated(bundle);
	TaskExecutorService.requestExecutorReference(TaskExecutorService.RETAIN_CURRENT_SERVICE_MODE, 
						     TaskExecutorService.RETAIN_CURRENT_AUTOEXEC_MODE, 
						     getActivity(), 
						     this, 
						     null);
    }
    
    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	mTaskExecutor = taskExecutor;
    }
}
