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

public class ExampleTwo extends Example{
    @Override
    public void onCreate(Bundle bundle){
	super.onCreate(bundle);
	((Button)findViewById(R.id.new_activity)).setVisibility(Button.GONE);
    }
}
