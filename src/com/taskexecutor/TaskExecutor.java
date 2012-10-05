package com.taskexecutor;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.Handler;

import com.taskexecutor.callbacks.CompleteCallback;
import com.taskexecutor.exceptions.NoQueuedTasksException;
import com.taskexecutor.exceptions.PendingTasksException;
import com.taskexecutor.runnables.Task;

public class TaskExecutor
{
	private boolean mAutoExecution = false;
	private static TaskExecutor mTaskExecutor = null;
	private Handler mHandler = new Handler();
	private ArrayList<Task> mQueue = new ArrayList<Task>();
	private ThreadPoolExecutor mTaskThreadExecutor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();

	private TaskExecutor()
	{
	}

	public static TaskExecutor getInstance()
	{
		if (mTaskExecutor == null)
			mTaskExecutor = new TaskExecutor();
		return mTaskExecutor;
	}

	/**
	 * @param autoExecution
	 * Start auto execution. If enabled executeQueue will be called every 500ms. Auto execution is off by default!
	 */
	public void setAutoExecution(final boolean autoExecution)
	{
		mAutoExecution = autoExecution;
		Runnable autoLoop = new Runnable()
		{
			@Override
			public void run()
			{

				if (!isExecuting())
				{
					try
					{
						executeQueue();
					} 
					catch (NoQueuedTasksException e)
					{
						e.printStackTrace();
					}
				}
				if (mAutoExecution)
					mHandler.postDelayed(this, 500);
			}
			
		};
		if (mAutoExecution)
			mHandler.post(autoLoop);
	}
	
	/**
	 * @param task
	 *            Provide a Task to be added to the queue pending execution.
	 * @param removeOnFail
	 *            By default Tasks will not be removed from the queue if they
	 *            fail to execute completely because of an exception. Pass true
	 *            to remove Tasks that experience exception.
	 * @throws IllegalStateException
	 * Queue is executing, please call stopExecution() first.
	 */
	public void addTaskToQueue(Task task, boolean removeOnFail) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() == 0)
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		task.setTaskExecutor(mTaskExecutor);
		task.setRemoveOnFail(removeOnFail);
		mQueue.add(task);
	}
	
	/**
	 * @param task
	 *            Provide a Task to be added to the queue pending execution.
	 * @throws IllegalStateException
	 * Queue is executing, please call stopExecution() first.
	 */
	public void addTaskToQueue(Task task) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() == 0)
			throw new IllegalStateException("Queue is executing, please call stopExecution() first.");
		task.setTaskExecutor(mTaskExecutor);
		mQueue.add(task);
	}

	/**
	 * @param task
	 * @throws IllegalStateException
	 * Queue is executing, please call stopExecution() first.
	 */
	public void removeTaskFromQueue(Task task) throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() == 0)
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
	 * executor queue for pending serial execution.
	 * 
	 * @throws NoQueuedTasksException
	 */
	public void executeQueue() throws NoQueuedTasksException
	{
		if (mQueue.size() == 0)
			throw new NoQueuedTasksException("No tasks are currently queued.");
		for (Task task : mQueue)
		{
			mTaskThreadExecutor.execute(task);
		}
	}

	/**
	 * Pause queue execution. If a task is currently being executed it likely
	 * will complete, but subsequent items will wait on the resumeQueue() call.
	 * If you have not called executeQueue(), should you be calling this method?
	 * 
	 * @throws IllegalStateException
	 *             Exception will be thrown if the queue isn't currently
	 *             executing.
	 */
	public void pause() throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() == 0)
			throw new IllegalStateException("Nothing is executing, why pause?");
		for (Task task : mQueue)
		{
			task.pause();
		}
	}

	/**
	 * Resume queue execution. If you have not called executeQueue(), should you
	 * be calling this method?
	 * 
	 * @throws IllegalStateException
	 *             Exception will be thrown if the queue isn't currently
	 *             executing.
	 */
	public void resume() throws IllegalStateException
	{
		if (mTaskThreadExecutor.getQueue().size() == 0)
			throw new IllegalStateException("Nothing is executing, why resume?");
		for (Task task : mQueue)
		{
			task.resume();
		}
	}

	/**
	 * If you've already started execution of the queue you you'll probably want
	 * to pause the queue prior to using this method. Call pause in onPause,
	 * then reset your callbacks in onResume and resume the queue.
	 * 
	 * @param completeCallback
	 */
	public void setCallbackForAllQueuedTasks(CompleteCallback completeCallback)
	{
		for (Task task : mQueue)
		{
			task.setCompleteCallback(completeCallback);
		}
	}
	
	/**
	 * @param TAG
	 * @return The Task for the specified TAG. This is useful is you want to specifically set a callback for a particular Task that is queued.
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

	/**
	 * @param forcedShutdown
	 *            If false and there are items in the queue this will throw a
	 *            PendingTasksException. Pass true if you want to forceShutdown.
	 * @throws PendingTasksException
	 */
	public void shutdownTaskExecutor(boolean forceShutdown) throws PendingTasksException
	{
		if (!forceShutdown && mQueue.size() > 0)
			throw new PendingTasksException("Tasks are in the queue.");
		mTaskThreadExecutor.shutdownNow();
		mTaskExecutor = null;
	}
}