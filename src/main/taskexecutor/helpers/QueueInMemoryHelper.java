package main.taskexecutor.helpers;
import java.util.Vector;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.runnables.Task;
import android.os.Handler;
/**
 * @author nseidm1
 * 
 */
public class QueueInMemoryHelper
{
    public static void setCallbackForAllQueuedTasks(Vector<Task> queue, TaskCompletedCallback completeCallback)
    {
	for (Task task : queue)
	{
	    task.setCompleteCallback(completeCallback);
	}
    }
    public static void setTaskExecutorForAllQueuedTasks(Vector<Task> queue, TaskExecutor taskExecutor)
    {
	for (Task task : queue)
	{
	    task.setTaskExecutor(taskExecutor);
	}
    }
    public static void setUIHandlerForAllQueuedTask(Vector<Task> queue, Handler uiHandler)
    {
	for (Task task : queue)
	{
	    task.setUiHandler(uiHandler);
	}
    }
}