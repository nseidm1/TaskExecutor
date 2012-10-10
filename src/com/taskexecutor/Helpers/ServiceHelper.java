package com.taskexecutor.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;

import com.google.gson.Gson;
import com.taskexecutor.TaskExecutor;
import com.taskexecutor.runnables.Task;

/**
 * @author nseidm1
 *
 */
public class ServiceHelper
{
	private static final String TASK_PERSISTENCE_DELIMER = ":TPD:";

	public static void retrieveTasksFromDisk(Context context, TaskExecutor taskExecutor) throws FileNotFoundException,
			IOException
	{
		taskExecutor.setQueue(getTasks(getFileContent(context.openFileInput("task.executor"))));
	}

	private static Vector<Task> getTasks(StringBuffer fileContent)
	{
		String[] taskFiles = fileContent.toString().split(TASK_PERSISTENCE_DELIMER);
		Vector<Task> tasks = new Vector<Task>();
		for (String taskFile : taskFiles)
		{
			tasks.add(new Gson().fromJson(taskFile, Task.class));
		}
		return tasks;
	}

	private static StringBuffer getFileContent(FileInputStream fis) throws IOException
	{
		StringBuffer fileContent = new StringBuffer("");
		byte[] buffer = new byte[1024];
		while ((fis.read(buffer)) != -1)
		{
			fileContent.append(new String(buffer));
		}
		return fileContent;
	}

	public static void persistQueueToDisk(Context context, TaskExecutor taskExecutor) throws IOException
	{
		String tasks = "";
		ArrayList<Task> localQueueCopy = new ArrayList<Task>(taskExecutor.getQueue());
		for (Task task : localQueueCopy)
		{
			tasks += new Gson().toJson(task) + TASK_PERSISTENCE_DELIMER;
		}
		FileOutputStream fos = context.openFileOutput("task.executor", Context.MODE_PRIVATE);
		fos.write(tasks.getBytes());
		fos.close();
	}

	public static void deleteSavedQueue(Context context)
	{
		new File(context.getFilesDir(), "task.executor").delete();
	}
}