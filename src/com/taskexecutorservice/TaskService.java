package com.taskexecutorservice;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.taskexecutorservice.callbacks.CompleteCallback;
import com.taskexecutorservice.exceptions.NoQueuedTasksException;
import com.taskexecutorservice.exceptions.PendingTasksException;
import com.taskexecutorservice.runnables.Task;

public class TaskService extends Service {
	private static TaskService mTaskService = null;
	private ArrayList<Task> mQueue = new ArrayList<Task>();
	private Executor mTaskExecutor = Executors.newSingleThreadExecutor();

	private TaskService() {
	}

	public static TaskService getInstance() {
		if (mTaskService == null) {
			mTaskService = new TaskService();
			mTaskService.startService(null);
		}
		return mTaskService;
	}

	/**
	 * @param task
	 *            Provide a Task to be added to the queue pending execution.
	 * @param removeOnFail
	 *            By default Tasks will not be removed from the queue if they
	 *            fail to execute properly (exception thrown). Pass true to
	 *            remove Tasks that fail to execute.
	 */
	public void addTaskToQueue(Task task, boolean removeOnFail) {
		task.setRemoveOnFail(removeOnFail);
		mQueue.add(task);
	}

	/**
	 * @param task
	 *            Remove a specific task from the queue.
	 */
	public void removeTaskFromQueue(Task task) {
		mQueue.remove(task);
	}

	/**
	 * Execute all tasks waiting in the queue. This adds all queued tasks to the
	 * executor queue for pending serial execution.
	 * 
	 * @throws NoQueuedTasksException
	 */
	public void executeQueue() throws NoQueuedTasksException {
		if (mQueue.size() == 0)
			throw new NoQueuedTasksException("No tasks are currently queued.");
		for (Task task : mQueue) {
			executeSpecificTask(task);
		}
	}

	/**
	 * Pause queue execution. If a task is currently being executed it likely
	 * will complete, but subsequent items will wait on the resumeQueue() call.
	 * If you have not called executeQueue(), should you be calling this method?
	 */
	public void pause() {

		for (Task task : mQueue) {
			task.pause();
		}
	}

	/**
	 * Resume queue execution. If you have not called executeQueue(), should you
	 * be calling this method?
	 */
	public void resume() {
		for (Task task : mQueue) {
			task.resume();
		}
	}

	/**
	 * If you've already started execution of the queue you you'll probably want
	 * to pause the queue prior to using this method. If a task is currently
	 * executing it will be difficult to assign a new completeCallback.
	 * 
	 * @param completeCallback
	 */
	public void setCallbackForAllQueuedTasks(CompleteCallback completeCallback) {
		for (Task task : mQueue) {
			task.setCompleteCallback(completeCallback);
		}
	}

	private void executeSpecificTask(Task task) {
		mTaskExecutor.execute(task);
	}

	/**
	 * @return return a count of items currently in the queue.
	 */
	public int getQueueCount() {
		return mQueue.size();
	}

	/**
	 * Clear all items from the queue. If tasks are currently being executed
	 * this will not prevent tasks from being executed.
	 */
	public void clearQueue() {
		mQueue.clear();
	}

	/**
	 * If you've called executeQueue(), then this method will attempt to stop
	 * executing queued tasks. If a task is currently being executed it will
	 * likely continue to completion. This will not remove items from the queue.
	 */
	public void stopExecution() {
		((ThreadPoolExecutor) mTaskExecutor).getQueue().clear();
	}

	/**
	 * @param forcedShutdown
	 *            If false and there are items in the queue this will throw a
	 *            PendingTasksException. Pass true if you want to forceShutdown.
	 * @throws PendingTasksException
	 */
	public void shutdownTaskService(boolean forceShutdown)
			throws PendingTasksException {
		if (!forceShutdown && mQueue.size() > 0)
			throw new PendingTasksException("Tasks are in the queue.");
		mTaskService = null;
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}