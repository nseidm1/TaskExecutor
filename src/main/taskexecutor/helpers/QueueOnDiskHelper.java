package main.taskexecutor.helpers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.runnables.Task;
import android.content.Context;
import com.google.gson.Gson;
/**
 * @author nseidm1
 * 
 */
public class QueueOnDiskHelper
{
    private static Gson mGson = new Gson();
    public static void retrieveTasksFromDisk(Context context, TaskExecutor taskExecutor) throws FileNotFoundException, IOException
    {
	taskExecutor.setQueue(getTasks(context));
    }
    private static Vector<Task> getTasks(Context context) throws FileNotFoundException, IOException
    {
	Vector<Task> taskArray = new Vector<Task>();
	File[] tasks = getTaskExecutorFilesDir(context).listFiles();
	for (File file : tasks)
	{
	    Task task = null;
	    try
	    {
		task = mGson.fromJson(getFileBytes(file), Task.class);
	    }
	    catch(Exception e)
	    {
		//Not a Task file.
	    }
	    if (task != null)
		taskArray.add(task);
	}
	return taskArray;
    }
    private static String getFileBytes(File task) throws IOException
    {
	FileInputStream fis = new FileInputStream(task);
	StringBuffer fileBytes = new StringBuffer("");
	byte[] buffer = new byte[1024];
	while ((fis.read(buffer)) != -1)
	{
	    fileBytes.append(new String(buffer));
	}
	fis.close();
	return fileBytes.toString();
    }
    public static void updateTasksOnDisk(Context context, TaskExecutor taskExecutor) throws IOException
    {
	Vector<Task> localQueueCopy = new Vector<Task>(taskExecutor.getQueue());// This
										// will
										// substantially
										// minimize
										// blocking
										// related
										// to
										// add/remove/clear
										// calls
										// on
										// the
										// queue
	addFilesInQueue(localQueueCopy, context);
	deleteFilesNotIntQueue(localQueueCopy, context);
    }
    private static void addFilesInQueue(Vector<Task> localQueueCopy, Context context) throws IOException
    {
	for (Task task : localQueueCopy)
	{
	    if (!new File(getTaskExecutorFilesDir(context), task.getTag()).exists())
	    {
		FileOutputStream fos = new FileOutputStream(new File(getTaskExecutorFilesDir(context), task.getTag()));
		fos.write(mGson.toJson(task).getBytes());
		fos.flush();
		fos.close();
	    }
	}
    }
    private static void deleteFilesNotIntQueue(Vector<Task> queue, Context context)
    {
	File[] tasks = context.getFilesDir().listFiles();
	for (int i = 0; i < tasks.length; i++)
	{
	    boolean delete = true;
	    for (Task task : queue)
	    {
		if (task.getTag().equalsIgnoreCase(tasks[i].getName()))
		    delete = false;
	    }
	    if (delete)
		tasks[i].delete();
	}
    }
    private static File getTaskExecutorFilesDir(Context context)
    {
	File projDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + "TaskExecutor");
	if (!projDir.exists())
	    projDir.mkdirs();
	return projDir;
    }
}