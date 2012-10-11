package main.taskexecutor.callbacks;
import android.os.Bundle;
/**
 * @author nseidm1
 * 
 */
public interface TaskCompletedCallback
{
    /**
     * @param task
     *            The Task that was being executed.
     * @param success
     *            If no exception was thrown this will be true and exception
     *            will be null, otherwise this will be false and exception will
     *            have information.
     * @param exception
     *            Null if the task did not throw an exception.
     * 
     */
    public void onTaskComplete(Bundle bundle, Exception exception);
}
