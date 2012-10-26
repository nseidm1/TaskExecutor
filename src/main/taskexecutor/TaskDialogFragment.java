package main.taskexecutor;

import main.taskexecutor.core.TaskExecutor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * The fragment will simply get a reference to the TaskExecutor via the calling Activity.
 * @author Noah Seidman
 */
public abstract class TaskDialogFragment extends DialogFragment{

    public static final String       TAG           = TaskDialogFragment.class.getName();
    protected           TaskExecutor mTaskExecutor = null;

    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	mTaskExecutor = ((TaskActivity)getActivity()).mTaskExecutor;
    }
}
