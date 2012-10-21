package main.taskexecutor.callbacks;

/**
 * @author Noah Seidman
 */
public interface TasksRestoredCallback{
    /**
     * This only gets called if Tasks are restored from disk and the Service is in 
     * SERVICE_MODE_CALLBACK_DEPENDENT mode.
     */
    public void notifyTasksHaveBeenRestored();
}
