package test.taskexecutor;

import android.annotation.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.CompoundButton.*;
import main.taskexecutor.*;
import main.taskexecutor.core.*;
import test.taskexecutor.tasks.*;

import android.view.View.OnClickListener;

public class Example extends TaskActivity implements OnClickListener{
    
    private   	 	 GetTask  postTask                  = null;
    private static final String   mDefaultUrl 	            = "http://m.google.com";
    private 		 String   mUrl                      = mDefaultUrl;
    private 		 boolean  mRemoveOnSuccess          = true;
    private 		 boolean  mRemoveOnException        = true;
    private 		 int      mDefaultDelay             = 0;
    private 		 Button   mExecute                  = null;
    private              Handler  mHandler                  = new Handler(Looper.getMainLooper());
    private              TextView mHardCallbackFeedbackArea = null;
    @TargetApi(11)
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	setContentView(R.layout.test);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	    getActionBar().setTitle("TaskExector Demonstration");
	}
	
	mHardCallbackFeedbackArea = (TextView)findViewById(R.id.hard_callback_beacon);

	((Button) findViewById(R.id.add_task_to_queue)).setOnClickListener(this);
	mExecute = ((Button) findViewById(R.id.execute));
	mExecute.setOnClickListener(this);
	((Button) findViewById(R.id.kill_example)).setOnClickListener(this);
	((Button) findViewById(R.id.empty_queue)).setOnClickListener(this);
	((Button) findViewById(R.id.new_activity)).setOnClickListener(this);
	((Button) findViewById(R.id.new_dialog)).setOnClickListener(this);
	
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
	if (!mUrl.equalsIgnoreCase(mDefaultUrl)){
	    url.setText(mUrl);
	}
	
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
	if (mDefaultDelay != 0){
	    delay.setText(Integer.toString(mDefaultDelay));
	}
	
	CheckBox shouldRemoveOnSuccess = ((CheckBox)findViewById(R.id.remove_on_success));
	shouldRemoveOnSuccess.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		mRemoveOnSuccess = isChecked;
	    }
	});
	shouldRemoveOnSuccess.setChecked(mRemoveOnSuccess);

	CheckBox shouldRemoveOnException = ((CheckBox)findViewById(R.id.remove_on_exception));
	shouldRemoveOnException.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		mRemoveOnException = isChecked;
	    }
	});
	shouldRemoveOnException.setChecked(mRemoveOnException);
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
	    mTaskExecutor.addTaskToQueue(postTask);
	    manageExecuteTasksButton();
	} else if (v.getId() == R.id.execute){
	    mTaskExecutor.executeQueue();
	} else if (v.getId() == R.id.kill_example){
	    System.runFinalization();
	    System.exit(0);
	    android.os.Process.killProcess(android.os.Process.myPid());
	} else if (v.getId() == R.id.empty_queue){
	    mTaskExecutor.clearQueue();
	    manageExecuteTasksButton();
	} else if (v.getId() == R.id.new_activity){
	    Intent intent = new Intent(this, Example.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
	    startActivity(intent);
	} else if (v.getId() == R.id.new_dialog){
	    ExampleDialog exampleDialog = ExampleDialog.newInstance();
	    exampleDialog.show(getSupportFragmentManager(), ExampleDialog.class.getName());
	}

    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	super.getTaskExecutorReference(taskExecutor);
	manageExecuteTasksButton();
    }
    
    @Override
    public void onTaskComplete(Bundle bundle, Exception exception){
	manageExecuteTasksButton();
	if (exception != null){
	    mHardCallbackFeedbackArea.setBackgroundColor(Color.RED);
	    mHardCallbackFeedbackArea.setTextColor(Color.BLACK);
	    mHandler.postDelayed(clearHardCallbackFeedbackArea, 1250);

	} else if (bundle != null){
	    mHardCallbackFeedbackArea.setBackgroundColor(Color.GREEN);
	    mHardCallbackFeedbackArea.setTextColor(Color.BLACK);
	    mHandler.postDelayed(clearHardCallbackFeedbackArea, 1250);
	}
	TaskDialogFragment dialog = (TaskDialogFragment) getSupportFragmentManager().findFragmentByTag(bundle.getString("CloseDialog"));
        if (dialog != null){
	    dialog.dismissAllowingStateLoss();
	}
    }
    
    private Runnable clearHardCallbackFeedbackArea = new Runnable(){
	@Override
	public void run() {
	    if (mHardCallbackFeedbackArea != null){
		mHardCallbackFeedbackArea.setBackgroundColor(Color.TRANSPARENT);
		mHardCallbackFeedbackArea.setTextColor(Color.WHITE);
	    }
	}
    };

    @Override
    public boolean allowTaskFiness(){
	return true;
    }

    @Override
    public int specifyServiceMode(){
	return TaskExecutorService.SERVICE_MODE_CALLBACK_DEPENDENT;
    }

    @Override
    public int specifyAutoexecMode(){
	return TaskExecutorService.AUTOEXEC_MODE_DISABLED;
    }
    
    @Override
    public boolean autoExecuteRestoredTasks(){
	//Aside from the javadoc it's worthwhile to mention here the following. 
	//Returning true here is only useful when the Service is in CALLBACK_DEPENDENT 
	//mode. Otherwise this callback will not be hit, and the Service will simply 
	//execute the Tasks automatically, without waiting, when Tasks are restore after 
	//a killed Service.
	return true;
    }
    @Override
    public void notifyTasksHaveBeenRestored(){
	super.notifyTasksHaveBeenRestored();
	manageExecuteTasksButton();
    }
    private void manageExecuteTasksButton(){
	if (mTaskExecutor.getQueueCount() == 0){
	    mExecute.setEnabled(false);
	} else{
	    mExecute.setEnabled(true);
	}
    }
}
