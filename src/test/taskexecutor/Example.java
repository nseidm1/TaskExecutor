package test.taskexecutor;

import java.io.IOException;
import main.taskexecutor.TaskExecutorActivity;
import main.taskexecutor.exceptions.DuplicateTagException;
import main.taskexecutor.runnables.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.taskexecutor.R;

public class Example extends TaskExecutorActivity
{
    private Button mTestButton1;
    private Button mTextButton2;
    private Button mTextButton3;
    @Override
    public void onCreate(Bundle bundle)
    {
	super.onCreate(bundle);
	setContentView(R.layout.test);
	mTestButton1 = (Button) findViewById(R.id.http_get_test_button);
	mTestButton1.setOnClickListener(this);
	mTextButton2 = (Button) findViewById(R.id.http_exception_test_button);
	mTextButton2.setOnClickListener(this);
	mTextButton3 = (Button) findViewById(R.id.http_delayed_get_test_button);
	mTextButton3.setOnClickListener(this);
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
		mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_GET_TASK, this, mHandler, true, true);
		mTaskExecutor.executeQueue();
	    }
	    catch (DuplicateTagException e)
	    {
		e.printStackTrace();
	    }
	}
	else if (v.getId() == R.id.http_exception_test_button)
	{
	    try
	    {
		Task EXAMPLE_HTTP_EXCEPTION_TASK = new Task()
		{
		    @Override
		    public void task() throws IOException
		    {
			HttpGet get = new HttpGet("http://m.google.com");
			AndroidHttpClient client = null;//NullPointerException
			HttpResponse response = client.execute(get);
		    }
		};	
		mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_EXCEPTION_TASK, this, mHandler, true, true);
		mTaskExecutor.executeQueue();
	    }
	    catch (DuplicateTagException e)
	    {
		e.printStackTrace();
	    }
	}
	else if (v.getId() == R.id.http_delayed_get_test_button)
	{
	    try
	    {
		Task EXAMPLE_HTTP_GET_TASK = new Task()
		{
		    @Override
		    public void task() throws IOException
		    {
			SystemClock.sleep(3000);
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
		mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_GET_TASK, this, mHandler, true, true);
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
	    Toast.makeText(this, TAG + " " + exception.toString(), Toast.LENGTH_SHORT).show();
	if (bundle != null)	
	    Toast.makeText(this, TAG + " " + bundle.getString("ResponseCode"), Toast.LENGTH_SHORT).show();
    }
}