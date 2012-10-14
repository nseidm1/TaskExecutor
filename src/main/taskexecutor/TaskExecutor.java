package main.taskexecutor;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import main.taskexecutor.callbacks.ServiceHelperCallback;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.helpers.QueueInMemoryHelper;
import main.taskexecutor.runnables.Task;
import android.os.Handler;
import android.os.Looper;
import main.taskexecutor.classes.Log;

/**
 * @author nseidm1
 */
public class TaskExecutor {
    private boolean mIsPaused = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Vector<Task> mQueue = new Vector<Task>();
    private ServiceHelperCallback mServiceHelperCallback;
    private ThreadPoolExecutor mTaskThreadExecutor = (ThreadPoolExecutor) Executors
	    .newFixedThreadPool(1);

    public TaskExecutor(ServiceHelperCallback serviceHelperCallback) {
	mServiceHelperCallback = serviceHelperCallback;
    }

    /**
     * @param pool
     *            By default Tasks are executed serially. You can execute Tasks
     *            concurrently if you'd like, but please consider the
     *            implications on finessing your Tasks to accommodate
     *            configurationChanges if your implementation is configured as
     *            such.
     */
    public void poolThreads(boolean pool) {
	if (pool) {
	    mTaskThreadExecutor = (ThreadPoolExecutor) Executors
		    .newCachedThreadPool();
	} else {
	    mTaskThreadExecutor = (ThreadPoolExecutor) Executors
		    .newFixedThreadPool(1);
	}
    }

    /**
     * @return Return if the queue's execution is currently paused.
     */
    public boolean isPaused() {
	return mIsPaused;
    }

    /**
     * @param task
     *            Provide a Task to be added to the queue pending execution.
     * @param taskCompletedCallback
     *            Provide an interface to callback when the Task has completed
     *            execution, you may supply null, but if you have allowFiness()
     *            enabled a callback will assigned at that time.
     */
    public void addTaskToQueue(Task task, TaskCompletedCallback taskCompletedCallback) {
	task.setCompleteCallback(taskCompletedCallback);
	task.setUiHandler(mHandler);
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
    public void removeTaskFromQueue(Task task) {
	mQueue.remove(task);
	queueModified();
    }

    /**
     * Execute all tasks in the queue that haven't been executed already.
     * 
     * @throws NoQueuedTasksException
     */
    public void executeQueue() {
	Log.d(TaskExecutor.class.getName(), "Execute " + mQueue.size()
		+ " Tasks");
	for (int i = 0; i < mQueue.size(); i++) {
	    if (!mTaskThreadExecutor.getQueue().contains(mQueue.get(i)))
		mTaskThreadExecutor.execute(mQueue.get(i));
	}
    }

    /**
     * Block the current running Task prior to the hard callback until
     * finessTasks() is called.
     */
    public void restrainTasks() {
	// Clear the callbacks to prevent leaks.
	// Pause all Tasks to block the next callback.
	QueueInMemoryHelper.setCallbackForAllQueuedTasks(mQueue, null);
	restrainAllQueuedTasks();
    }

    private void restrainAllQueuedTasks() {
	if (!mIsPaused) {
	    mIsPaused = true;
	    for (int i = 0; i < mQueue.size(); i++){
		mQueue.get(i).pause();
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
     */
    public void finessTasks(TaskCompletedCallback taskCompleteCallback) {
	QueueInMemoryHelper.setCallbackForAllQueuedTasks(mQueue, taskCompleteCallback);
	unrestrainAllQueuedTasks();
    }

    private void unrestrainAllQueuedTasks() {
	if (mIsPaused) {
	    mIsPaused = false;
	    for (int i = 0; i < mQueue.size(); i++){
		mQueue.get(i).resume();
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
    public Task findTaskForTag(String TAG) {
	for (Task task : mQueue) {
	    if (task.getTag().equals(TAG))
		return task;
	}
	return null;
    }

    /**
     * @return return a count of items currently in the queue.
     */
    public int getQueueCount() {
	return mQueue.size();
    }

    /**
     * @return A reference to the existing Task queue.
     */
    public Vector<Task> getQueue() {
	return mQueue;
    }

    /**
     * @param queue
     *            Set the Task queue. Typically used when restoring the
     *            TaskExecutor for the persisted instance on disk.
     */
    public void setQueue(Vector<Task> queue) {
	mQueue = queue;
	queueModified();
    }

    /**
     * Clear all items from the queue. If tasks are currently being executed
     * this will not prevent tasks from being executed.
     */
    public void clearQueue() {
	mQueue.clear();
	queueModified();
    }

    private void queueModified() {
	if (mServiceHelperCallback != null)
	    mServiceHelperCallback.queueModified();
    }
}