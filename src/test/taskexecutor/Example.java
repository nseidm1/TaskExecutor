package test.taskexecutor;

import main.taskexecutor.R;
import main.taskexecutor.TaskExecutorActivity;
import main.taskexecutor.TaskExecutorService;
import test.taskexecutor.tasks.PostTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Example extends TaskExecutorActivity implements OnClickListener {
    @Override
    public void onCreate(Bundle bundle) {
	super.onCreate(bundle);
	setContentView(R.layout.test);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    getActionBar().setTitle("TaskExector Demonstration");
	}
	((Button) findViewById(R.id.http_get_test_button))
		.setOnClickListener(this);
	((Button) findViewById(R.id.http_exception_test_button))
		.setOnClickListener(this);
	((Button) findViewById(R.id.http_delayed_get_test_button))
		.setOnClickListener(this);
	((Button) findViewById(R.id.twenty_sec_http_delayed_get_test_button))
		.setOnClickListener(this);
	((Button) findViewById(R.id.execute)).setOnClickListener(this);
	((Button) findViewById(R.id.kill_example)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
	if (v.getId() == R.id.http_get_test_button) {
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 0);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	} else if (v.getId() == R.id.http_exception_test_button) {
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 0);
	    bundle.putString(PostTask.URL, "null");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	} else if (v.getId() == R.id.http_delayed_get_test_button) {
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 3000);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	} else if (v.getId() == R.id.twenty_sec_http_delayed_get_test_button) {
	    PostTask postTask = new PostTask();
	    Bundle bundle = new Bundle();
	    bundle.putInt(PostTask.DELAY, 20000);
	    bundle.putString(PostTask.URL, "http://m.google.com");
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	} else if (v.getId() == R.id.execute) {
	    mTaskExecutor.executeQueue();
	} else if (v.getId() == R.id.kill_example) {
	    System.runFinalization();
	    System.exit(0);
	}
    }

    @Override
    public void onTaskComplete(Bundle bundle, Exception exception) {
	if (exception != null) {
	    Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT)
		    .show();
	} else if (bundle != null) {
	    Toast.makeText(this, bundle.getString("ResponseCode"),
		    Toast.LENGTH_SHORT).show();
	}
    }

    @Override
    public boolean allowTaskFiness() {
	return true;
    }

    @Override
    public int specifyServiceMode() {
	return TaskExecutorService.CALLBACK_DEPENDENT;
    };
}