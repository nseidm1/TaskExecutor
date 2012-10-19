package main.taskexecutor.helpers;

import java.io.IOException;
import main.taskexecutor.classes.Log;
import main.taskexecutor.core.TaskExecutor;
import main.taskexecutor.core.TaskExecutorService;

public class QueueToDiskTask implements Runnable{
    private TaskExecutor        mTaskExecutor         = null;
    private TaskExecutorService mTaskExecutorService  = null;

    public QueueToDiskTask(TaskExecutor        taskExecutor,
	    		   TaskExecutorService taskExecutorService){
	mTaskExecutor        = taskExecutor;
	mTaskExecutorService = taskExecutorService;
    }

    @Override
    public void run(){
	updateTasksOnDisk();
    }

    private void updateTasksOnDisk(){
	try{
	    QueueOnDiskHelper.updateTasksOnDisk(mTaskExecutorService, mTaskExecutor);
	}catch (IOException e){
	    e.printStackTrace();
	    Log.e(TaskExecutorService.class.getName(), "Error saving existing queue.");
	}
    }
}