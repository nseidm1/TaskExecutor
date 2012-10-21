package main.taskexecutor.callbacks;

import android.os.Bundle;

/**
 * @author Noah Seidman
 */
public interface TaskCompletedCallback {
    /**
     * @param bundle
     * The bundle from the Task that completed execution.
     * @param exception
     * Null if the task did not throw an exception.
     */
    public void onTaskComplete(Bundle bundle, Exception exception);
}
