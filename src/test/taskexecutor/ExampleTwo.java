package test.taskexecutor;

import main.taskexecutor.R;
import main.taskexecutor.core.TaskExecutor;
import test.taskexecutor.loaders.ExampleTwoLoader;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Button;
import android.widget.Toast;

public class ExampleTwo extends Example implements LoaderCallbacks<Bundle>{
    
    public static final String 	       TOAST_MESSAGE     = "TOAST_MESSAGE";
    private             Loader<Bundle> mExampleTwoLoader = null;
    private             LoaderManager  mLoaderManager    = null;
    
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	//Hide the new Activity button.
	((Button)findViewById(R.id.new_activity)).setVisibility(Button.GONE);
	mLoaderManager = getSupportLoaderManager();
    }

    @Override
    public Loader<Bundle> onCreateLoader(int arg0, Bundle arg1){
	return mExampleTwoLoader;
    }

    @Override
    public void onLoadFinished(Loader<Bundle> exampleTwoLoader, Bundle results){
	if (results != null){
	    if (results.getString(TOAST_MESSAGE) != null){
		Toast.makeText(this, results.getString(TOAST_MESSAGE), Toast.LENGTH_SHORT).show();
	    }
	}
    }
    
    @Override
    public void onLoaderReset(Loader<Bundle> exampleTwoLoader){
	Toast.makeText(this, "Loader Reset", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getTaskExecutorReference(TaskExecutor taskExecutor){
	super.getTaskExecutorReference(taskExecutor);
	//Restore the reference to an existing loader, or create a new one.
	mExampleTwoLoader = mLoaderManager.getLoader(1);
	if (mExampleTwoLoader == null)
	    mExampleTwoLoader = new ExampleTwoLoader(this, mTaskExecutor);
	this.getSupportLoaderManager().initLoader(1/*Only one loader*/, null/*No bundle needed*/, this);
    }
}