package main.taskexecutor.callbacks;

public interface ServiceCallbackDependentHelperCallback {
    /**
     * This is only called if the service is in CALLBACK_DEPENDENT mode. It's
     * called when Tasks have been successfully restored from disk and are
     * awaiting to be assigned an Activity callback before execution.
     */
    public void tasksHaveBeenRestored();
}
