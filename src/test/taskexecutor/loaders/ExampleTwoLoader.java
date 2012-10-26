package test.taskexecutor.loaders;

import main.taskexecutor.TaskLoader;
import main.taskexecutor.core.TaskExecutor;
import test.taskexecutor.ExampleTwo;
import android.content.Context;
import android.os.Bundle;

public class ExampleTwoLoader extends TaskLoader<Bundle>{

    public ExampleTwoLoader(Context context, TaskExecutor taskExecutor){
        super(context, taskExecutor);
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