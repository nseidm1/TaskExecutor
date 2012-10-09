package com.taskexecutor;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.Handler;

import com.taskexecutor.callbacks.TaskCompletedCallback;
import com.taskexecutor.runnables.Task;

public class TaskExecutor
{
	private boolean mIsPaused = false;
	private ArrayList<Task> mQueue = new ArrayList<Task>();
	private ThreadPoolExecutor mTaskThreadExecutor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();

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
	 * @param uiHandler
	 *            Provide a UI handler for the Task to post.
	 * @param removeOnException
	 *            Should the Task be removed from the queue if it fails to
	 *            execute completely because of an exception?
	 * @param removeOnSuccess
	 *            Should the Task be removed from the queue if it completes
	 *            without exception?
	 * @throws IllegalStateException
	 *             Queue is executing, please call stopExecution() first.
	 */
	public void addTaskToQueue(Task task, Handler uiHandler, boolean removeOnException, boolean removeOnSuccess)
			throws IllegalStateException
	{
		if (isExecuting())
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		task.setUiHandler(uiHandler);
		task.setTaskExecutor(this);
		task.setRemoveOnException(removeOnException);
		task.setRemoveOnSuccess(removeOnSuccess);
		mQueue.add(task);
	}

	/**
	 * @param task
	 *            Provide an existing Task to remove from the queue. You can use
	 *            findTaskForTag to locate a particular Task.
	 * @throws IllegalStateException
	 *             Queue is executing, please call stopExecution() first.
	 */
	public void removeTaskFromQueue(Task task) throws IllegalStateException
	{
		if (isExecuting())
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		mQueue.remove(task);
	}

	/**
	 * @return true if the queue is currently executing.
	 */
	public boolean isExecuting()
	{
		return mTaskThreadExecutor.getQueue().size() != 0;
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
			mTaskThreadExecutor.execute(task);
		}
	}

	/**
	 * Pause queue execution if applicable. If a task is currently being
	 * executed it will complete, but but the CompleteExecution callback will
	 * block until resumeQueue() is called; this gives the opportunity to reset
	 * the Task callback and the UI handler in onResume().
	 */
	public void onPause()
	{
		if (isExecuting() && !mIsPaused)
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
	 * UI handler just to be sure.
	 * 
	 * @param callCompleteCallback
	 *            Provide the taskCompleteCallback so your Tasks can report back
	 *            to the activity.
	 * @param uiHandler
	 *            Provide a uiHandler so your task can post back to the current
	 *            UI thread.
	 */
	public void onResume(TaskCompletedCallback taskCompleteCallback, Handler uiHandler)
	{
		setCallbackForAllQueuedTasks(taskCompleteCallback);
		setUIHandlerForAllQueuedTask(uiHandler);
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
	 * If you've already started execution of the queue you you'll probably want
	 * to pause the queue prior to using this method. Call pause in onPause,
	 * then reset your callbacks in onResume and resume the queue.
	 * 
	 * @param completeCallback
	 */
	public void setCallbackForAllQueuedTasks(TaskCompletedCallback completeCallback)
	{
		for (Task task : mQueue)
		{
			task.setCompleteCallback(completeCallback);
		}
	}

	/**
	 * If you expect your UI handler reference to be destroyed you can reset it
	 * using this method.
	 * 
	 * @param uiHandler
	 *            Provide a reference to a UI handler.
	 */
	public void setUIHandlerForAllQueuedTask(Handler uiHandler)
	{
		for (Task task : mQueue)
		{
			task.setUiHandler(uiHandler);
		}
	}

	/**
	 * This will set the removeOnException flag for all queued Tasks.
	 */
	public void setRemoveOnExceptionForAllQueuedTasks()
	{
		for (Task task : mQueue)
		{
			task.setRemoveOnException(true);
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
	public ArrayList<Task> getQueue()
	{
		return mQueue;
	}

	/**
	 * @param queue
	 *            Set the Task queue. Typically used when restoring the
	 *            TaskExecutor for the persisted instance on disk.
	 */
	public void setQueue(ArrayList<Task> queue)
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
	}

	/**
	 * If you've called executeQueue(), then this method will attempt to stop
	 * executing queued tasks by removing them from the executor's queue. If a
	 * task is currently being executed it will likely continue to completion.
	 * This will not remove items from the queue, use clearQueue() for that.
	 */
	public void stopExecution() throws UnsupportedOperationException
	{
		mTaskThreadExecutor.getQueue().clear();
	}
}