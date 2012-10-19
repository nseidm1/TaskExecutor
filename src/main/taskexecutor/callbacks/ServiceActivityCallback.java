package main.taskexecutor.callbacks;

/**
 * @author Noah Seidman
 */
public interface ServiceActivityCallback{
    /**
     * This only gets called if the Tasks are restored from disk and the Service is in CALLBACK_DEPENDENT mode. 
     * If the Service is in CALLBACK_INCONSIDERATE 
     * mode they will just be executed by the Service without a valid ui callback.
     */
    public void tasksHaveBeenRestored();
}
