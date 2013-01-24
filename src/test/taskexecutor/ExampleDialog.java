package test.taskexecutor;

import main.taskexecutor.TaskActivity;
import main.taskexecutor.TaskDialogFragment;
import main.taskexecutor.core.Task;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

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
    public void onActivityCreated(Bundle bundle){
	super.onActivityCreated(bundle);
	mTaskExecutor.addTaskToQueue(new DialogTask());
	mTaskExecutor.executeQueue();
    }
    
    public static class DialogTask extends Task{
	@Override
	public void task() throws Exception{
	    Thread.sleep(1000);
	    Bundle updateBundle = new Bundle();
	    updateBundle.putString("TOAST", "Almost There");
	    postUpdate(updateBundle);
	    Thread.sleep(2000);
	    //Do stuff here.
	    getMainBundle().putString("CloseDialog", ExampleDialog.class.getName());
	}

	@Override
	public boolean queueResultsIfNoActivity() {
	    return true;
	}
    }
}
