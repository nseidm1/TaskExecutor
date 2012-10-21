package test.taskexecutor;

import android.app.*;
import android.os.*;
import main.taskexecutor.*;
import main.taskexecutor.core.*;

public class ExampleDialog extends TaskDialogFragment{

    private TaskActivity mActivity;
    public static ExampleDialog newInstance(){
	return new ExampleDialog();
    }
    @Override
    public Dialog onCreateDialog(Bundle bundle){
	setRetainInstance(true);
	mActivity = (TaskActivity) getActivity();
	ProgressDialog dialog = new ProgressDialog(mActivity);
	dialog.setTitle("Example Dialog");
	dialog.setMessage("The activity will close this dialog in 3 seconds");
	return dialog;
    }
    
    @Override
    public void onDestroyView(){
	if (getDialog() != null && getRetainInstance())
	    getDialog().setDismissMessage(null);
	super.onDestroyView();
    }
    
    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	super.getTaskExecutorReference(taskExecutor);
	mTaskExecutor.addTaskToQueue(new DialogTask());
	mTaskExecutor.executeQueue();
    }
    
    public static class DialogTask extends Task{
	public void task() throws Exception{
	    SystemClock.sleep(3000);
	    //Do stuff here.
	    getBundle().putString("CloseDialog", ExampleDialog.class.getName());
	}
    }
}
