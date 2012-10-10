package main.taskexecutor;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import main.taskexecutor.callbacks.ServiceHelperCallback;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.exceptions.DuplicateTagException;
import main.taskexecutor.helpers.QueueInMemoryHelper;
import main.taskexecutor.runnables.Task;
import android.os.Handler;
/**
 * @author nseidm1
 * 
 */
public class TaskExecutor
{
    private boolean mIsPaused = false;
    private Handler mHandler = new Handler();
    private Vector<Task> mQueue = new Vector<Task>();
    private ServiceHelperCallback mServiceHelperCallback;
    private ExecutorService mTaskThreadExecutor = Executors.newSingleThreadExecutor();
    public TaskExecutor()
    {
    }
    public TaskExecutor(ServiceHelperCallback serviceHelperCallback)
    {
	mServiceHelperCallback = serviceHelperCallback;
    }
    /**
     * @param threadPoolExecutor
     *            Specify a custom thread pool executor. The default is a single
     *            thread pool maximizing the effectiveness of pausing.
     */
    public void specifyExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
	mTaskThreadExecutor = threadPoolExecutor;
    }
    /**
     * @return Return if the queue's execution is currently paused.
     */
    public boolean isPaused()
    {
	return mIsPaused;
    }
    /**
     * @param task
     *            Provide a Task to be added to the queue pending execution.
     * @param taskCompletedCallback
     *            Provide an interface to callback when the Task has completed
     *            execution. You can pass null if no callback is needed.
     * @param uiHandler
     *            Provide a UI handler for the Task to post. You can pass null
     *            if nothing needs to post back to the UI.
     * @param removeOnException
     *            Should the Task be removed from the queue if it fails to
     *            execute completely because of an exception?
     * @param removeOnSuccess
     *            Should the Task be removed from the queue if it completes
     *            without exception?
     * @throws DuplicateTagException
     *             Because Tasks are persisted to disk TAGs need to be a unique
     *             identifier.
     */
    public void addTaskToQueue(Task task, TaskCompletedCallback taskCompletedCallback, boolean removeOnException, boolean removeOnSuccess) throws DuplicateTagException
    {
	QueueInMemoryHelper.checkForDuplicateTasks(mQueue, task);
	task.setCompleteCallback(taskCompletedCallback);
	task.setUiHandler(mHandler);
	task.setRemoveOnException(removeOnException);
	task.setRemoveOnSuccess(removeOnSuccess);
	task.setTaskExecutor(this);
	mQueue.add(task);
	queueModified();
    }
    /**
     * @param task
     *            Provide an existing Task to remove from the queue. You can use
     *            findTaskForTag to locate a particular Task. This will not stop
     *            a Task from executing if you've already called executeQueue().
     */
    public void removeTaskFromQueue(Task task)
    {
	mQueue.remove(task);
	queueModified();
    }
    /**
     * Execute all tasks waiting in the queue. This adds all queued tasks to the
     * executor queue for serial execution. Serial execution is the default, you
     * can set the executor to pool if desired using specifyExecutor().
     * 
     * @throws NoQueuedTasksException
     */
    public void executeQueue()
    {
	for (Task task : mQueue)
	{
	    task.setFuture(mTaskThreadExecutor.submit(task));
	}
    }
    /**
     * Pause queue execution if applicable. If a task is currently being
     * executed it will complete, but but the CompleteExecution callback will
     * block until resumeQueue() is called..
     */
    public void onPause()
    {
	// Clear the callbacks to prevent leaks.
	// Pause all Tasks to block the next callback.
	QueueInMemoryHelper.setTaskExecutorForAllQueuedTasks(mQueue, null);
	QueueInMemoryHelper.setUIHandlerForAllQueuedTask(mQueue, null);
	QueueInMemoryHelper.setTaskExecutorForAllQueuedTasks(mQueue, null);
	pauseAllQueuedTasks();
    }
    private void pauseAllQueuedTasks()
    {
	if (!mIsPaused)
	{
	    mIsPaused = true;
	    for (Task task : mQueue)
	    {
		task.pause();
	    }
	}
    }
    /**
     * Resume Task execution. Provide a fresh taskCompleteCallback and a fresh
     * UI handler if desired.
     * 
     * @param callCompleteCallback
     *            Provide the taskCompleteCallback so your Tasks can report back
     *            to the activity.
     * @param uiHandler
     *            Provide a uiHandler so your task can post back to the current
     *            UI thread.
     */
    public void onResume(TaskCompletedCallback taskCompleteCallback)
    {
	// Reset the task complete callback.
	// Reset the ui handler.
	// Reset the reference to the TaskExecutor.
	QueueInMemoryHelper.setCallbackForAllQueuedTasks(mQueue, taskCompleteCallback);
	QueueInMemoryHelper.setUIHandlerForAllQueuedTask(mQueue, mHandler);
	QueueInMemoryHelper.setTaskExecutorForAllQueuedTasks(mQueue, this);
	unPauseAllQueuedTasks();
    }
    private void unPauseAllQueuedTasks()
    {
	if (mIsPaused)
	{
	    mIsPaused = false;
	    for (Task task : mQueue)
	    {
		task.resume();
	    }
	}
    }
    /**
     * @param TAG
     *            Provide the TAG of the Task you want to find.
     * @return The Task for the specified TAG. Null is returned if no Task is
     *         found. This is useful is you want to specifically set a callback
     *         for a particular Task that is queued.
     */
    public Task findTaskForTag(String TAG)
    {
	for (Task task : mQueue)
	{
	    if (task.getTag().equals(TAG))
		return task;
	}
	return null;
    }
    /**
     * @return return a count of items currently in the queue.
     */
    public int getQueueCount()
    {
	return mQueue.size();
    }
    /**
     * @return A reference to the existing Task queue.
     */
    public Vector<Task> getQueue()
    {
	return mQueue;
    }
    /**
     * @param queue
     *            Set the Task queue. Typically used when restoring the
     *            TaskExecutor for the persisted instance on disk.
     */
    public void setQueue(Vector<Task> queue)
    {
	mQueue = queue;
    }
    /**
     * Clear all items from the queue. If tasks are currently being executed
     * this will not prevent tasks from being executed.
     */
    public void clearQueue()
    {
	mQueue.clear();
	queueModified();
    }
    /**
     * If you've called executeQueue(), then this method will attempt to stop
     * executing queued tasks by canceling Future Tasks. If a task is currently
     * being executed you can attempt interruption using the interrupt
     * parameter. This will not remove items from the queue, use clearQueue()
     * for that.
     */
    public void stopExecution(boolean interrupt) throws UnsupportedOperationException
    {
	for (Task task : mQueue)
	{
	    if (task.getFuture() != null)
		task.getFuture().cancel(interrupt);
	}
    }
    private void queueModified()
    {
	if (mServiceHelperCallback != null)
	    mServiceHelperCallback.queueModified();
    }
}