package test.taskexecutor;

import main.taskexecutor.R;
import main.taskexecutor.TaskActivity;
import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.core.TaskExecutorService;
import test.taskexecutor.tasks.GetTask;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.*;

public class Example extends TaskActivity implements OnClickListener{
    
    private   	 	 GetTask  postTask                  = null;
    private static final String   mDefaultUrl 	            = "http://m.google.com";
    private 		 String   mUrl                      = mDefaultUrl;
    private 		 boolean  mRemoveOnSuccess          = true;
    private 		 boolean  mRemoveOnException        = true;
    private 		 int      mDefaultDelay             = 0;
    private              int      mDefaultInterrupt         = -1;
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
		try{
		    if (s.toString() == null || s.toString().equalsIgnoreCase("")){
			mDefaultDelay = 0;
			return;
		    }
		    mDefaultDelay = Integer.parseInt(s.toString());
		}catch(NumberFormatException e){
		    mDefaultDelay = 0;
		}
	    }
	});
	if (mDefaultDelay != 0){
	    delay.setText(Integer.toString(mDefaultDelay));
	}
	
	EditText interrupt = (EditText) findViewById(R.id.interrupt);
	interrupt.setInputType(InputType.TYPE_CLASS_PHONE);
	interrupt.addTextChangedListener(new TextWatcher(){
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count){}
	    @Override
	    public void afterTextChanged(Editable s){
		try{
		    if (s.toString() == null || s.toString().equalsIgnoreCase("")){
			mDefaultInterrupt = -1;
			return;
		    }
		    mDefaultInterrupt = Integer.parseInt(s.toString());
		}catch(NumberFormatException e){
		    mDefaultInterrupt = -1;
		}
	    }
	});
	if (mDefaultInterrupt != -1){
	    interrupt.setText(Integer.toString(mDefaultInterrupt));
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
	    postTask.setMainBundle(bundle);
	    mTaskExecutor.addTaskToQueue(postTask);
	    manageExecuteTasksButton();
	} else if (v.getId() == R.id.execute){
	    mTaskExecutor.setInterruptTaskAfter(mDefaultInterrupt);
	    mTaskExecutor.executeQueue();
	} else if (v.getId() == R.id.kill_example){
	    System.runFinalization();
	    System.exit(0);
	    android.os.Process.killProcess(android.os.Process.myPid());
	} else if (v.getId() == R.id.empty_queue){
	    mTaskExecutor.clearQueue();
	    manageExecuteTasksButton();
	} else if (v.getId() == R.id.new_activity){
	    Intent intent = new Intent(this, ExampleTwo.class);
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
	if (!processException(exception))
	    processBundle(bundle);
	processCloseDialogRequest(bundle);
    }
    
    @Override
    public void onTaskUpdate(Bundle bundle){
	processToast(bundle);
    }

    private void processToast(Bundle bundle){
	if (bundle != null){
	    String toast = bundle.getString("TOAST");
	    if (toast != null){
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	    }
	}
    }
    
    private void processBundle(Bundle bundle){
	if (bundle != null){
	    mHardCallbackFeedbackArea.setBackgroundColor(Color.GREEN);
	    mHardCallbackFeedbackArea.setTextColor(Color.BLACK);
	    mHandler.postDelayed(clearHardCallbackFeedbackArea, 1250);
	}	
    }

    private boolean processException(Exception exception){
	if (exception != null){
	    mHardCallbackFeedbackArea.setBackgroundColor(Color.RED);
	    mHardCallbackFeedbackArea.setTextColor(Color.BLACK);
	    mHardCallbackFeedbackArea.setText(exception.toString());
	    mHandler.postDelayed(clearHardCallbackFeedbackArea, 1250);
	    return true;
	} 	
	return false;
    }

    private void processCloseDialogRequest(Bundle bundle){
	if (bundle != null){
	    Fragment fragment = getSupportFragmentManager().findFragmentByTag(bundle.getString("CloseDialog"));
	    if (fragment != null){
		((DialogFragment) fragment).dismissAllowingStateLoss();
	    }
	}
    }

    private Runnable clearHardCallbackFeedbackArea = new Runnable(){
	@Override
	public void run() {
	    if (mHardCallbackFeedbackArea != null){
		mHardCallbackFeedbackArea.setBackgroundColor(Color.TRANSPARENT);
		mHardCallbackFeedbackArea.setTextColor(Color.WHITE);
		mHardCallbackFeedbackArea.setText("Hard Callback Feedback Area");
	    }
	}
    };

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
	//Returning true here is only useful when the Service is in SERVICE_MODE_CALLBACK_DEPENDENT 
	//mode. Otherwise this value is not useful.
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
