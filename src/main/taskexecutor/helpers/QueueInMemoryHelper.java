package main.taskexecutor.helpers;

import java.util.Vector;

import main.taskexecutor.Task;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import android.os.Handler;

/**
 * @author nseidm1
 */
public class QueueInMemoryHelper{
    public static void setCallbackForAllQueuedTasks(Vector<Task> queue, TaskCompletedCallback completeCallback){
	for (int i = 0; i < queue.size(); i++){
	    queue.get(i).setCompleteCallback(completeCallback);
	}
    }

    public static void setTaskExecutorForAllQueuedTasks(Vector<Task> queue, TaskExecutor taskExecutor){
	for (int i = 0; i < queue.size(); i++){
	    queue.get(i).setTaskExecutor(taskExecutor);
	}
    }

    public static void setUIHandlerForAllQueuedTask(Vector<Task> queue, Handler uiHandler){
	for (int i = 0; i < queue.size(); i++){
	    queue.get(i).setUiHandler(uiHandler);
	}
    }
}