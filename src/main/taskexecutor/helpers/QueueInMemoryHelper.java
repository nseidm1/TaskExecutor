package main.taskexecutor.helpers;

import java.util.Vector;

import main.taskexecutor.core.Task;
import main.taskexecutor.core.TaskExecutor;

/**
 * @author Noah Seidman
 */
public class QueueInMemoryHelper{
    public static void setTaskExecutorForAllQueuedTasks(Vector<Task> queue, TaskExecutor taskExecutor){
	for (int i = 0; i < queue.size(); i++){
	    queue.get(i).setTaskExecutor(taskExecutor);
	}
    }
}
