package test.android.taskexecutor;

import java.io.IOException;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.TaskExecutorService;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskExecutorReferenceCallback;
import main.taskexecutor.exceptions.DuplicateTagException;
import main.taskexecutor.runnables.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.taskexecutor.R;

public class Example extends FragmentActivity implements TaskExecutorReferenceCallback, TaskCompletedCallback, OnClickListener
{
    private TaskExecutor mTaskExecutor;
    private Button mTestButton1;
    private Handler mUIHandler = new Handler();
    @Override
    public void onCreate(Bundle bundle)
    {
	super.onCreate(bundle);
	setContentView(R.layout.test);
	mTestButton1 = (Button) findViewById(R.id.http_get_test_button);
	mTestButton1.setOnClickListener(this);
	TaskExecutorService.requestExecutorReference(this, this);
    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor)
    {
	mTaskExecutor = taskExecutor;	
    }

    @Override
    public void onClick(View v)
    {
	if (v.getId() == R.id.http_get_test_button)
	{
	    try
	    {
		Task EXAMPLE_HTTP_GET_TASK = new Task()
		{
		    @Override
		    public void task() throws IOException
		    {
			HttpGet get = new HttpGet("http://m.google.com");
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpResponse response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();
			Bundle bundle = new Bundle();
			bundle.putString("ResponseCode", "Response Code: " + responseCode);
			setBundle(bundle);
			setTag("EXAMPLE_HTTP_GET_TASK");
		    }
		};	
		mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_GET_TASK, this, mUIHandler, true, true);
		mTaskExecutor.executeQueue();
	    }
	    catch (DuplicateTagException e)
	    {
		e.printStackTrace();
	    }
	}
    }
    
    @Override
    public void onTaskComplete(Bundle bundle, String TAG, boolean success, Exception exception)
    {
	if (exception != null)
	{
	    Toast.makeText(this, TAG + " " + exception.toString(), Toast.LENGTH_SHORT).show();
	}
	else
	{
	    Toast.makeText(this, TAG + " " + bundle.getString("ResponseCode"), Toast.LENGTH_SHORT).show();
	}
    }
}