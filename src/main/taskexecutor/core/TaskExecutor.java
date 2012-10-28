package main.taskexecutor.core;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import main.taskexecutor.callbacks.ServiceExecutorCallback;
import main.taskexecutor.callbacks.TaskCompletedCallback;
import main.taskexecutor.callbacks.TaskUpdateCallback;
import main.taskexecutor.classes.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

/**
 * @author Noah Seidman
 */
public class TaskExecutor{
            Handler                         mHandler                   = new Handler(Looper.getMainLooper());
            TaskCompletedCallback           mTaskCompletedCallback     = null;
	    TaskUpdateCallback              mTaskUpdateCallback        = null;
	    int                             mInterruptTasksAfter       = -1;
	    Vector<Pair<Bundle, Exception>> mPendingCompletedTasks     = new Vector<Pair<Bundle, Exception>>();
    private Vector<Task>                    mQueue                     = new Vector<Task>();
    private ServiceExecutorCallback         mServiceHelperCallback     = null;
    private ThreadPoolExecutor              mTaskExecutor              = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public TaskExecutor(ServiceExecutorCallback serviceHelperCallback){
	mServiceHelperCallback = serviceHelperCallback;
    }

    /**
     * @param pool
     * By default Tasks are executed serially. You can execute Tasks
     * concurrently if you'd like, but please consider the implications such as ordered 
     * ui callbacks.
     */
    public void poolThreads(boolean pool){
	if (pool){
	    mTaskExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	} else{
	    mTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	}
    }
    
    /**
     * @param task
     * Provide a Task to be added to the queue pending execution.
     * @param taskCompletedCallback
     * Provide an interface to callback when the Task has completed execution,
     * you may supply null, but if you have allowFiness() enabled a callback
     * will assigned at that time.
     */
    public void addTaskToQueue(Task task){
	task.setTaskExecutor(this);
	mQueue.add(task);
	queueModified();
    }

    /**
     * @param task
     * Provide an existing Task to remove from the queue. You can use
     * findTaskForTag to locate a particular Task. This will not stop a Task
     * from executing if you've already called executeQueue().
     */
    public void removeTaskFromQueue(Task task){
	mQueue.remove(task);
	queueModified();
    }

    /**
     * Execute all tasks in the queue that haven't been executed already.
     */
    public void executeQueue(){
	Log.d(TaskExecutor.class.getName(), "Execute " + mQueue.size() + " Tasks");
	for (Task task : mQueue){
	    if (!mTaskExecutor.getQueue().contains(task)){
		executeTask(task);
	    }
	}
    }
    
    /**
     * This method directly executes the Task, bypassing the queue. Useful for the 
     * abstract TaskLoader. The Task will not be persisted to disk, no ui callbacks
     * will be posted unless a reference to the TaskExecutor is set.
     * @param task
     * @return Future<?>
     * For optimal use in a Loader, return a reference to the Future.
     * 
     */
    public Future<?> executeTask(Task task){
	Future<?> future = mTaskExecutor.submit(task);
	setInterruptorIfActive(future);
	return future;
    }
    
    /**
     * @param seconds
     * -1 disables this feature, and it's disabled by default. Set this value 
     * to milliseconds of time. After that amount of time elapses your Task 
     * will be interrupted. If it has yet to execute it will not execute. If the 
     * Task has already begun executing it will be interrupted, which may prove 
     * useful if the Task has a blocking operation like network communication.
     */
    public void setInterruptTaskAfter(int interruptTasksAfter){
	mInterruptTasksAfter = interruptTasksAfter;
    }
    
    private void setInterruptorIfActive(final Future<?> future){
	if (mInterruptTasksAfter != -1){
	    mHandler.postDelayed(new Runnable(){
		@Override
		public void run(){
		    future.cancel(true);
		}
	    }, mInterruptTasksAfter);
	}
    }

    /**
     * Clear the callbacks to prevent leaks.
     */
    public void clean(){
	mTaskCompletedCallback = null;
	mTaskUpdateCallback = null;
    }

    /**
     * @param taskCompletedCallback
     * @param taskUpdateCallback
     */
    public void finess(TaskCompletedCallback taskCompletedCallback, TaskUpdateCallback TaskUpdateCallback){
	//Set the callbacks and post any queued Task completed results.
	mTaskCompletedCallback = taskCompletedCallback;
	mTaskUpdateCallback = TaskUpdateCallback;
	postQueuedCompletedTasks();
    }

    private void postQueuedCompletedTasks(){
	for (Pair<Bundle, Exception> pendingCompletedTask : mPendingCompletedTasks)
	    mTaskCompletedCallback.onTaskComplete(pendingCompletedTask.first, pendingCompletedTask.second);
	mPendingCompletedTasks.clear();	
    }

    /**
     * @param TAG
     * Provide the TAG of the Task you want to find.
     * @return The Task for the specified TAG. Null is returned if no Task is
     * found. This is useful is you want to specifically set a callback for a
     * particular Task that is queued.
     */
    public Task findTaskForTag(String TAG){
	for (Task task : mQueue) {
	    if (task.getTag().equals(TAG))
		return task;
	}
	return null;
    }

    /**
     * @return return a count of items currently in the queue.
     */
    public int getQueueCount(){
	return mQueue.size();
    }

    /**
     * @return A reference to the existing Task queue.
     */
    public Vector<Task> getQueue(){
	return mQueue;
    }

    /**
     * @param queue
     * Set the Task queue. Typically used when restoring the TaskExecutor for
     * the persisted instance on disk.
     */
    public void setQueue(Vector<Task> queue){
	mQueue = queue;
	queueModified();
    }

    /**
     * Clear all items from the queue. If tasks are currently being executed
     * this will not prevent tasks from being executed.
     */
    public void clearQueue(){
	mQueue.clear();
	queueModified();
    }

    private void queueModified(){
	mServiceHelperCallback.queueModified();
    }
}
