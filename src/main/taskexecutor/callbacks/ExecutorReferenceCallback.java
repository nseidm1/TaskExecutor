package main.taskexecutor.callbacks;

import main.taskexecutor.TaskExecutor;

/**
 * @author nseidm1
 */
public interface ExecutorReferenceCallback {
    public void getTaskExecutorReference(TaskExecutor taskExecutor);
}