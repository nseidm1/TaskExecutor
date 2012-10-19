package main.taskexecutor.callbacks;

import main.taskexecutor.core.TaskExecutor;

/**
 * @author Noah Seidman
 */
public interface ExecutorReferenceCallback {
    public void getTaskExecutorReference(TaskExecutor taskExecutor);
}
