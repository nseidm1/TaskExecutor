package test.taskexecutor;
import main.taskexecutor.R;
import main.taskexecutor.TaskExecutorActivity;
import main.taskexecutor.TaskExecutorService;
import test.taskexecutor.tasks.PostTask;
import android.os.Bundle;
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
    private Button mTextButton6;
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
	mTextButton6 = (Button) findViewById(R.id.kill_example);
	mTextButton6.setOnClickListener(this);
    }
    @Override
    public void onClick(View v)
    {
	if (v.getId() == R.id.http_get_test_button)
	{
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 0);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	}
	else if (v.getId() == R.id.http_exception_test_button)
	{
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 0);
	    bundle.putString(PostTask.URL, "null");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	}
	else if (v.getId() == R.id.http_delayed_get_test_button)
	{
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 3000);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	}
	else if (v.getId() == R.id.twenty_sec_http_delayed_get_test_button)
	{
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 20000);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	}
	else if (v.getId() == R.id.execute)
	{
	    mTaskExecutor.executeQueue();
	}
	else if (v.getId() == R.id.kill_example)
	{
	    System.exit(0);
	}
    }
    @Override
    public void onTaskComplete(Bundle bundle, Exception exception)
    {
	if (exception != null)
	{
	    Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show();
	}
	else if (bundle != null)
	{
	    Toast.makeText(this, bundle.getString("ResponseCode"), Toast.LENGTH_SHORT).show();
	}
    }
    @Override
    public boolean allowTaskFiness()
    {
	return true;
    }
    @Override
    public int specifyServiceMode()
    {
	return TaskExecutorService.CALLBACK_DEPENDENT;
    };
}