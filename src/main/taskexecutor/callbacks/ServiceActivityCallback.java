package main.taskexecutor.callbacks;

public interface ServiceActivityCallback{
    /**
     * This only gets called if the Tasks are restored from disk and the Service is in CALLBACK_DEPENDENT mode.
     */
    public void tasksHaveBeenRestored();
}
