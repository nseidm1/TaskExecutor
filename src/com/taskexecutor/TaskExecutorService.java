package com.taskexecutor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.taskexecutor.callbacks.ServiceReferenceCallback;
import com.taskexecutor.runnables.Task;

public class TaskExecutorService extends Service
{
	private static final String TASK_PERSISTENCE_DELIMER = ":TPD:";
	private TaskExecutor mTaskExecutor = new TaskExecutor();
	private static SoftReference<ServiceReferenceCallback> mSoftCallback;

	public static void requestReference(Context context, ServiceReferenceCallback serviceReferenceCallback)
	{
		mSoftCallback = new SoftReference<ServiceReferenceCallback>(serviceReferenceCallback);
		context.startService(new Intent(context, TaskExecutorService.class));
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		mSoftCallback.get().getServiceReference(this);
		retrieveTasksFromDisk();
		mTaskExecutor.executeQueue();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mTaskExecutor.onPause();
		mTaskExecutor.stopExecution();
		persistTasksToDisk();
	}

	private void retrieveTasksFromDisk()
	{
		try
		{
			FileInputStream fis = openFileInput("task.executor");
			StringBuffer fileContent = getFileContent(fis);
			ArrayList<Task> tasks = getTasks(fileContent);
			mTaskExecutor.setQueue(tasks);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private ArrayList<Task> getTasks(StringBuffer fileContent)
	{
		String[] taskFiles = fileContent.toString().split(TASK_PERSISTENCE_DELIMER);
		ArrayList<Task> tasks = new ArrayList<Task>();
		for (String taskFile : taskFiles)
		{
			tasks.add(new Gson().fromJson(taskFile, Task.class));
		}
		return tasks;
	}

	private StringBuffer getFileContent(FileInputStream fis) throws IOException
	{
		StringBuffer fileContent = new StringBuffer("");

		byte[] buffer = new byte[1024];
		while ((fis.read(buffer)) != -1)
		{
			fileContent.append(new String(buffer));
		}
		return fileContent;
	}

	private void persistTasksToDisk()
	{
		try
		{
			String tasks = "";
			for (Task task : mTaskExecutor.getQueue())
			{
				tasks += new Gson().toJson(task) + TASK_PERSISTENCE_DELIMER;
			}
			FileOutputStream fos = openFileOutput("task.executor", Context.MODE_PRIVATE);
			fos.write(tasks.getBytes());
			fos.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
