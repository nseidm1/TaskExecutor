package main.taskexecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View.OnClickListener;
/**
 * @author nseidm1
 * 
 */
public abstract class TaskExecutorActivity extends FragmentActivity implements TaskCompletedCallback, TaskExecutorReferenceCallback, OnClickListener
{
    protected TaskExecutor mTaskExecutor;
    @Override
    public void onCreate(Bundle bundle)
    {
	super.onCreate(bundle);
	TaskExecutorService.requestExecutorReference(this, this);
    }
    @Override
    public void onPause()
    {
	super.onPause();
	mTaskExecutor.onPause();
    }
    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor)
    {
	mTaskExecutor = taskExecutor;
	mTaskExecutor.onResume(this);
    }
}