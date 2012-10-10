package main.taskexecutor.helpers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.runnables.Task;
import android.content.Context;
import android.util.Log;
/**
 * @author nseidm1
 * 
 */
public class QueueOnDiskHelper
{
    public static boolean retrieveTasksFromDisk(Context context, TaskExecutor taskExecutor) throws FileNotFoundException, IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
    {
	Vector<Task> tasks = getTasks(context);
	if (tasks.size() > 0)
	{
	    taskExecutor.setQueue(tasks);
	    return true;
	}
	return false;
    }
    public static void updateTasksOnDisk(Context context, TaskExecutor taskExecutor) throws IOException
    {
	Vector<Task> localQueueCopy = new Vector<Task>(taskExecutor.getQueue());
	addFilesInQueue(localQueueCopy, context);
	deleteFilesNotIntQueue(localQueueCopy, context);
    }
    // ////////////////////////////////////////////////////
    // //////////Private methods hereforth/////////////////
    // ////////////////////////////////////////////////////
    private static Vector<Task> getTasks(Context context) throws FileNotFoundException, IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
    {
	Vector<Task> taskArray = new Vector<Task>();
	File[] tasks = getTaskExecutorFilesDir(context).listFiles();
	Log.d(QueueOnDiskHelper.class.getName(), "Number of Tasks being restored: " + tasks.length);
	for (File file : tasks)
	{
	    InputStream is = new FileInputStream(file);
	    StringBuffer fileContent = new StringBuffer("");

	    byte[] buffer = new byte[1024];
	    int bytesRead;
	    while ((bytesRead = is.read(buffer)) != -1) 
	    {
	        fileContent.append(new String(buffer, 0, bytesRead, "UTF-8"));
	    }
	    is.close();

	    String className = fileContent.toString();
	    Class<?> clazzName = Class.forName(className);
	    Constructor<?>[] constructors = (Constructor<?>[]) clazzName.getConstructors();
	    Task task = (Task) constructors[0].newInstance(file.getName());
//	    Log.d(QueueOnDiskHelper.class.getName(), task.getTag() + "Restored");
	    if (task != null)
		taskArray.add(task);
	}
	return taskArray;
    }
    private static void addFilesInQueue(Vector<Task> localQueueCopy, Context context) throws IOException
    {
	for (Task task : localQueueCopy)
	{
	    File taskFile = new File(getTaskExecutorFilesDir(context), task.getTag());
	    if (!taskFile.exists())
	    {
		FileOutputStream fos = new FileOutputStream(taskFile);
		String className = task.getClass().getName();
		fos.write(className.getBytes());
		fos.flush();
		fos.close();
		Log.d(QueueOnDiskHelper.class.getName(), task.getTag() + " written to disk");
	    }
	}
    }
    private static void deleteFilesNotIntQueue(Vector<Task> queue, Context context)
    {
	File[] tasks = getTaskExecutorFilesDir(context).listFiles();
	for (int i = 0; i < tasks.length; i++)
	{
	    boolean delete = true;
	    for (Task task : queue)
	    {
		if (task.getTag().equalsIgnoreCase(tasks[i].getName()))
		    delete = false;
	    }
	    if (delete)
	    {
		Log.d(QueueOnDiskHelper.class.getName(), tasks[i].getName() + " deleted from disk");
		tasks[i].delete();
	    }
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