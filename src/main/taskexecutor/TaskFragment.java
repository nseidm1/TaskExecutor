package main.taskexecutor;

import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.exceptions.CalledFromWrongActivityException;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * The fragment will simply get a reference to the TaskExecutor via the calling Activity.
 * @author Noah Seidman
 */
public abstract class TaskFragment extends Fragment{

    public    static final String       TAG           = TaskFragment.class.getName();
    protected              TaskExecutor mTaskExecutor = null;
    
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	Activity activity = getActivity();
	if (activity instanceof TaskActivity){
	    mTaskExecutor = ((TaskActivity)getActivity()).mTaskExecutor;
	}else{
	    throw new CalledFromWrongActivityException("The calling Activity must be an instance of TaskActivity.");
	}
    }
}
