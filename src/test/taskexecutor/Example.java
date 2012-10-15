package test.taskexecutor;

import main.taskexecutor.R;
import main.taskexecutor.TaskExecutorActivity;
import main.taskexecutor.TaskExecutorService;
import test.taskexecutor.tasks.GetTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class Example extends TaskExecutorActivity implements OnClickListener{
    
            GetTask postTask           = null;
    private String  mUrl               = "http://m.google.com";
    private boolean mRemoveOnSuccess   = true;
    private boolean mRemoveOnException = true;
    private int     mDefaultDelay      = 0;
    private Button  mExecute           = null;
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	setContentView(R.layout.test);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	    getActionBar().setTitle("TaskExector Demonstration");
	}

	((Button) findViewById(R.id.add_task_to_queue)).setOnClickListener(this);
	mExecute = ((Button) findViewById(R.id.execute));
	mExecute.setOnClickListener(this);
	((Button) findViewById(R.id.kill_example)).setOnClickListener(this);
	((Button) findViewById(R.id.empty_queue)).setOnClickListener(this);
	
	EditText url = ((EditText)findViewById(R.id.url));
	url.addTextChangedListener(new TextWatcher()
	{
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count){}
	    @Override
	    public void afterTextChanged(Editable s){
		if (s.toString() == null || s.toString().equals("")){
		    mUrl = "http://m.google.com";
		    return;
		}
		mUrl = s.toString();
	    }
	});
	EditText delay = ((EditText)findViewById(R.id.delay));
	delay.setInputType(InputType.TYPE_CLASS_PHONE);
	delay.addTextChangedListener(new TextWatcher(){
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count){}
	    @Override
	    public void afterTextChanged(Editable s){
		if (s.toString() == null || s.toString().equalsIgnoreCase("")){
		    mDefaultDelay = 0;
		    return;
		}
		mDefaultDelay = Integer.parseInt(s.toString());
	    }
	});
	CheckBox shouldRemoveOnSuccess = ((CheckBox)findViewById(R.id.remove_on_success));
	shouldRemoveOnSuccess.setChecked(mRemoveOnSuccess);
	shouldRemoveOnSuccess.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		mRemoveOnSuccess = isChecked;
	    }
	});
	CheckBox shouldRemoveOnException = ((CheckBox)findViewById(R.id.remove_on_exception));
	shouldRemoveOnException.setChecked(mRemoveOnException);
	shouldRemoveOnException.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		mRemoveOnException = isChecked;
	    }
	});
    }
    @Override
    public void onClick(View v){
	if (v.getId() == R.id.add_task_to_queue){
	    postTask = new GetTask();
	    postTask.setShouldRemoveFromQueueOnException(mRemoveOnException);
	    postTask.setShouldRemoveFromQueueOnSuccess(mRemoveOnSuccess);
	    Bundle bundle = new Bundle();
	    bundle.putInt(GetTask.DELAY, mDefaultDelay);
	    bundle.putString(GetTask.URL, mUrl);
	    postTask.setBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask, this);
	    mExecute.setEnabled(true);
	} else if (v.getId() == R.id.execute){
	    mTaskExecutor.executeQueue();
	} else if (v.getId() == R.id.kill_example){
	    System.runFinalization();
	    System.exit(0);
	} else if (v.getId() == R.id.empty_queue){
	    mTaskExecutor.clearQueue();
	    mExecute.setEnabled(false);
	}

    }

    @Override
    public void onTaskComplete(Bundle bundle, Exception exception){
	if (mTaskExecutor.getQueueCount() == 0){
	    mExecute.setEnabled(false);
	}
	if (exception != null){
	    Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show();
	} else if (bundle != null){
	    Toast.makeText(this, bundle.getString("ResponseCode"), Toast.LENGTH_SHORT).show();
	}
    }

    @Override
    public boolean allowTaskFiness(){
	return true;
    }

    @Override
    public int specifyServiceMode(){
	return TaskExecutorService.CALLBACK_DEPENDENT;
    }

    @Override
    public boolean autoExecuteRestoredTasks(){
	return true;
    }
    @Override
    public void tasksHaveBeenRestored(){
	super.tasksHaveBeenRestored();
	if (mTaskExecutor.getQueueCount() == 0){
	    mExecute.setEnabled(false);
	} else{
	    mExecute.setEnabled(true);
	}
    }
}