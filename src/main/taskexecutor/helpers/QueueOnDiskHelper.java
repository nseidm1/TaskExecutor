package main.taskexecutor.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import main.taskexecutor.TaskExecutor;
import main.taskexecutor.persistence.PersistenceObject;
import main.taskexecutor.runnables.Task;
import android.content.Context;
import android.os.Parcel;
import main.taskexecutor.classes.Log;

/**
 * @author nseidm1
 */
public class QueueOnDiskHelper {
    /**
     * @param context
     *            Provide a context.
     * @param taskExecutor
     *            Provide a reference to the TaskExecutor.
     * @return Returns true if Tasks were retrived from disk.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static boolean retrieveTasksFromDisk(Context context,
	    TaskExecutor taskExecutor) throws FileNotFoundException,
	    IOException, IllegalArgumentException, InstantiationException,
	    IllegalAccessException, InvocationTargetException,
	    NoSuchMethodException, ClassNotFoundException {
	Vector<Task> tasks = getTasks(context, taskExecutor);
	if (tasks.size() > 0) {
	    taskExecutor.setQueue(tasks);
	    return true;
	}
	return false;
    }

    /**
     * @param context
     *            Provide a context.
     * @param taskExecutor
     *            Provide a reference to the TaskExecutor.
     * @throws IOException
     */
    public static void updateTasksOnDisk(Context context,
	    TaskExecutor taskExecutor) throws IOException {
	Vector<Task> localQueueCopy = new Vector<Task>(taskExecutor.getQueue());
	addFilesInQueue(localQueueCopy, context);
	deleteFilesNotIntQueue(localQueueCopy, context);
    }

    // ////////////////////////////////////////////////////
    // //////////Private methods hereforth/////////////////
    // ////////////////////////////////////////////////////
    private static Vector<Task> getTasks(Context context,
	    TaskExecutor taskExecutor) throws FileNotFoundException,
	    IOException, IllegalArgumentException, InstantiationException,
	    IllegalAccessException, InvocationTargetException,
	    NoSuchMethodException, ClassNotFoundException {
	Vector<Task> taskArray = new Vector<Task>();
	File[] tasks = getTaskExecutorFilesDir(context).listFiles();
	Log.d(QueueOnDiskHelper.class.getName(),
		"Number of Tasks being restored: " + tasks.length);
	for (File file : tasks) {
	    FileInputStream fIn = new FileInputStream(file);
	    byte[] buffer = new byte[(int) file.length()];
	    int length = fIn.read(buffer);
	    Log.d(QueueOnDiskHelper.class.getName(), "Buffer Length: " + length);
	    fIn.close();
	    if (length != file.length())
		throw new IndexOutOfBoundsException();
	    Parcel parcel = Parcel.obtain();
	    parcel.unmarshall(buffer, 0, buffer.length);
	    parcel.setDataPosition(0);
	    Log.d(QueueOnDiskHelper.class.getName(), "Data Available: "
		    + parcel.dataAvail());
	    Log.d(QueueOnDiskHelper.class.getName(),
		    "Data Size: " + parcel.dataSize());
	    PersistenceObject persistenceObject = PersistenceObject.CREATOR
		    .createFromParcel(parcel);
	    parcel.recycle();
	    String className = persistenceObject.getClassName();
	    Class<?> clazzName = Class.forName(className);
	    Constructor<?> constructor = clazzName.getConstructor();
	    Task task = (Task) constructor.newInstance();
	    task.setBundle(persistenceObject.getBundle());
	    task.setTag(persistenceObject.getTag());
	    task.setShouldRemoveFromQueueOnException(persistenceObject
		    .getShouldRemoveFromQueueOnException());
	    task.setShouldRemoveFromQueueOnSuccess(persistenceObject
		    .getShouldRemoveFromQueueOnSuccess());
	    task.setTaskExecutor(taskExecutor);
	    Log.d(QueueOnDiskHelper.class.getName(), task.getTag()
		    + " restored");
	    if (task != null)
		taskArray.add(task);
	}
	return taskArray;
    }

    private static void addFilesInQueue(Vector<Task> localQueueCopy,
	    Context context) throws IOException {
	for (Task task : localQueueCopy) {
	    File taskFile = new File(getTaskExecutorFilesDir(context),
		    task.getTag());
	    if (!taskFile.exists()) {
		FileOutputStream fos = new FileOutputStream(taskFile);
		PersistenceObject persistenceObject = new PersistenceObject(
			task.getClass().getName(), task.getBundle(),
			task.getTag());
		Parcel parcel = Parcel.obtain();
		persistenceObject.writeToParcel(parcel, 0);
		try {
		    fos.write(parcel.marshall());
		    Log.d(QueueOnDiskHelper.class.getName(), task.getTag()
			    + " written to disk");
		} finally {
		    fos.flush();
		    fos.close();
		    parcel.recycle();
		}
	    }
	}
    }

    private static void deleteFilesNotIntQueue(Vector<Task> queue,
	    Context context) {
	File[] tasks = getTaskExecutorFilesDir(context).listFiles();
	for (int i = 0; i < tasks.length; i++) {
	    boolean delete = true;
	    for (Task task : queue) {
		if (task.getTag().equalsIgnoreCase(tasks[i].getName()))
		    delete = false;
	    }
	    if (delete) {
		Log.d(QueueOnDiskHelper.class.getName(), tasks[i].getName()
			+ " deleted from disk");
		tasks[i].delete();
	    }
	}
    }

    private static File getTaskExecutorFilesDir(Context context) {
	File projDir = new File(context.getFilesDir().getAbsolutePath()
		+ File.separator + "TaskExecutor");
	if (!projDir.exists())
	    projDir.mkdirs();
	return projDir;
    }
}