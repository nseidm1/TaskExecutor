package main.taskexecutor.helpers;

import java.util.Vector;

import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.core.Task;
import main.taskexecutor.core.TaskExecutor;
import android.os.Handler;

/**
 * @author Noah Seidman
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
