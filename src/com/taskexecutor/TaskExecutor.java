package com.taskexecutor;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.taskexecutor.callbacks.TaskCompletedCallback;
import com.taskexecutor.exceptions.PendingTasksException;
import com.taskexecutor.runnables.Task;

public class TaskExecutor
{
	private boolean mIsPaused = false;
	private boolean mPermitCallbackIfPaused = false;
	private ArrayList<Task> mQueue = new ArrayList<Task>();
	private ThreadPoolExecutor mTaskThreadExecutor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();

	/**
	 * @param setAutoExecution
	 *            Do you want the queue to automatically start executing if not
	 *            currently executing? You can set this to false at a later
	 *            time, which may be useful if you want to stage Tasks to be
	 *            executed in a batch.
	 */
	public TaskExecutor()
	{
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
	 * @param continueExecutionIfPaused
	 *            Default is false and if set to true Task execution will not
	 *            pause with the activity. Please carefully consider this if
	 *            your callbacks invoke anything in the ui!
	 */
	public void setPermitCallbackIfPaused(boolean permitCallbackIfPaused)
	{
		mPermitCallbackIfPaused = permitCallbackIfPaused;
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
	 * @param removeOnFail
	 *            By default Tasks will not be removed from the queue if they
	 *            fail to execute completely because of an exception. Pass true
	 *            to remove Tasks that experience exception.
	 * @throws IllegalStateException
	 *             Queue is executing, please call stopExecution() first.
	 */
	public void addTaskToQueue(Task task, boolean removeOnException) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() != 0)
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		task.setTaskExecutor(this);
		task.setRemoveOnException(removeOnException);
		mQueue.add(task);
	}

	/**
	 * @param task
	 *            Provide a Task to be added to the queue pending execution.
	 * @throws IllegalStateException
	 *             Queue is executing, please call stopExecution() first.
	 */
	public void addTaskToQueue(Task task) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() != 0)
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		task.setTaskExecutor(this);
		mQueue.add(task);
	}

	/**
	 * @param task
	 * @throws IllegalStateException
	 *             Queue is executing, please call stopExecution() first.
	 */
	public void removeTaskFromQueue(Task task) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() != 0)
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		mQueue.remove(task);
	}
	
	/**
	 * @param task
	 * A task to execute immediately, bypassing the queue.
	 */
	public void runTask(Task task)
	{
		mTaskThreadExecutor.execute(task);
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
	 * executor queue for pending serial execution.
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
	 * the Task callback in onResume().
	 */
	public void onPause()
	{
		if (!mPermitCallbackIfPaused && isExecuting() && !mIsPaused)
		{
			mIsPaused = true;
			for (Task task : mQueue)
			{
				task.pause();
			}
		}
	}

	/**
	 * Resume queue execution from a paused state if applicable.
	 */
	public void onResume(TaskCompletedCallback callCompleteCallback)
	{
		setCallbackForAllQueuedTasks(callCompleteCallback);

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
		if (getQueueCount() != 0)
		{
			for (Task task : mQueue)
			{
				task.setCompleteCallback(completeCallback);
			}
		}
	}
	
	/**
	 * This will set the removeOnException flag for all queued Tasks.
	 */
	public void setRemoveOnExceptionForAllQueuedTasks()
	{
		if (getQueueCount() != 0)
		{
			for (Task task : mQueue)
			{
				task.setRemoveOnException(true);
			}
		}
	}

	/**
	 * @param TAG
	 * @return The Task for the specified TAG. This is useful is you want to
	 *         specifically set a callback for a particular Task that is queued.
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
	 * Clear all items from the queue. If tasks are currently being executed
	 * this will not prevent tasks from being executed.
	 */
	public void clearQueue()
	{
		mQueue.clear();
	}

	/**
	 * If you've called executeQueue(), then this method will attempt to stop
	 * executing queued tasks. If a task is currently being executed it will
	 * likely continue to completion. This will not remove items from the queue.
	 */
	public void stopExecution() throws UnsupportedOperationException
	{
		mTaskThreadExecutor.getQueue().clear();
	}
}