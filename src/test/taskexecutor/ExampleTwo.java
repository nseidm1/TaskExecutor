package test.taskexecutor;

import android.os.*;
import android.widget.*;
import main.taskexecutor.*;

public class ExampleTwo extends Example{
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	//Hide the new Activity button.
	((Button)findViewById(R.id.new_activity)).setVisibility(Button.GONE);
    }
}
