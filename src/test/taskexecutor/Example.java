package test.taskexecutor;
import java.io.IOException;
import main.taskexecutor.R;
import main.taskexecutor.TaskExecutorActivity;
import main.taskexecutor.runnables.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
public class Example extends TaskExecutorActivity implements OnClickListener
{
    private Button mTestButton1;
    private Button mTextButton2;
    private Button mTextButton3;
    private Button mTextButton4;
    private Button mTextButton5;
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
	mTextButton4 = (Button) findViewById(R.id.twenty_sec_http_delayed_get_test_button);
	mTextButton4.setOnClickListener(this);
	mTextButton5 = (Button) findViewById(R.id.execute);
	mTextButton5.setOnClickListener(this);
    }
    @Override
    public void onClick(View v)
    {
	if (v.getId() == R.id.http_get_test_button)
	{
	    mTaskExecutor.addTaskToQueue(new EXAMPLE_HTTP_GET_TASK("EXAMPLE_HTTP_GET_TASK"), this);
	}
	else if (v.getId() == R.id.http_exception_test_button)
	{
	    Task EXAMPLE_HTTP_EXCEPTION_TASK = new Task("EXAMPLE_HTTP_EXCEPTION_TASK")
	    {
		@SuppressWarnings("null")
		@Override
		public void task() throws IOException
		{
		    HttpGet get = new HttpGet("http://m.google.com");
		    AndroidHttpClient client = null;// NullPointerException
		    client.execute(get);
		}
	    };
	    mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_EXCEPTION_TASK, this);
	}
	else if (v.getId() == R.id.http_delayed_get_test_button)
	{
	    Task EXAMPLE_HTTP_GET_DELAYED_TASK = new Task("EXAMPLE_HTTP_GET_DELAYED_TASK")
	    {
		@Override
		public void task() throws IOException
		{
		    SystemClock.sleep(3000);
		    HttpGet get = new HttpGet("http://m.google.com");
		    AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		    HttpResponse response = client.execute(get);
		    client.close();
		    int responseCode = response.getStatusLine().getStatusCode();
		    Bundle bundle = new Bundle();
		    bundle.putString("ResponseCode", "Response Code: " + responseCode);
		    setBundle(bundle);
		}
	    };
	    mTaskExecutor.addTaskToQueue(EXAMPLE_HTTP_GET_DELAYED_TASK, this);
	}
	else if (v.getId() == R.id.twenty_sec_http_delayed_get_test_button)
	{
	    Task EXAMPLE_20_SEC_HTTP_GET_DELAYED_TASK = new Task("EXAMPLE_20_SEC_HTTP_GET_DELAYED_TASK")
	    {
		@Override
		public void task() throws IOException
		{
		    SystemClock.sleep(20000);
		    HttpGet get = new HttpGet("http://m.google.com");
		    AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		    HttpResponse response = client.execute(get);
		    client.close();
		    int responseCode = response.getStatusLine().getStatusCode();
		    Bundle bundle = new Bundle();
		    bundle.putString("ResponseCode", "Response Code: " + responseCode);
		    setBundle(bundle);
		}
	    };
	    mTaskExecutor.addTaskToQueue(EXAMPLE_20_SEC_HTTP_GET_DELAYED_TASK, this);
	}
	else if (v.getId() == R.id.execute)
	{
	    mTaskExecutor.executeQueue();
	}
    }
    @Override
    public void onTaskComplete(Bundle bundle, String TAG, Exception exception)
    {
	if (exception != null)
	{
	    Toast.makeText(this, TAG + " " + exception.toString(), Toast.LENGTH_SHORT).show();
	}
	else if (bundle != null)
	{
	    Toast.makeText(this, TAG + " " + bundle.getString("ResponseCode"), Toast.LENGTH_SHORT).show();
	}
    }
    public static class EXAMPLE_HTTP_GET_TASK extends Task
    {
	public EXAMPLE_HTTP_GET_TASK(String tag)
	{
	    super(tag);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void task() throws IOException
	{
	    HttpGet get = new HttpGet("http://m.google.com");
	    AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	    HttpResponse response = client.execute(get);
	    client.close();
	    int responseCode = response.getStatusLine().getStatusCode();
	    Bundle bundle = new Bundle();
	    bundle.putString("ResponseCode", "Response Code: " + responseCode);
	    setBundle(bundle);
	}
    };
}