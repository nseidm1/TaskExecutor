package test.taskexecutor.loaders;

import main.taskexecutor.TaskActivity;
import main.taskexecutor.TaskLoader;
import test.taskexecutor.ExampleTwo;
import android.os.Bundle;

public class ExampleTwoLoader extends TaskLoader<Bundle>{

    public ExampleTwoLoader(TaskActivity activity){
        super(activity);
    }
    @Override
    protected Bundle loaderTask() throws Exception {
	Bundle bundle = new Bundle();
	bundle.putString(ExampleTwo.TOAST_MESSAGE, "Loader has processed Data");
	Thread.sleep(3000);
	return bundle;
    }
    @Override
    protected void onStartLoading(){
	if (mData != null) {
	    deliverResult(mData);
	}
	else{
	    forceLoad();
	}
    }
}