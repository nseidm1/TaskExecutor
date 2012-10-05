TaskExecutor
===================

The included abstract TaskExecutorActivity class makes for easy use. Simply extend the class and utilize the protected mTaskExecutor reference to execute Tasks. Tasks 
Are runnables with additional helper methods to facilitate management by the TaskExecutor facility. Tasks take a TaskCompletedCallback which the abstract TaskExecutorActivity implements. You will 
receive callbacks to this interface when executed Tasks are completed.
<br><br>
By default executed Tasks are paused when the activity is paused. If a Task is currently being executed it will continue, but the runnable will be blocked prior to the callback. 
This is the default behavior to facilitate configurationChange events and activity destruction; in onResume all Tasks will 
have their callback reset, and the queue will be unblocked.

<b>Tasks</b><br>
Tasks are extended Runnables, and instead of overriding run you'll override the task method. The Task constructor takes a reference to the TaskCompletedCallback which the abstract activity implements.
<br><br>
Tasks have 5 public methods:<br>
1) setCompleteCallback(TaskCompletedCallback completeCallback)<br>
2) setTaskExecutor(TaskExecutor taskExecutor)<br>
3) setRemoveOnException(Boolean removeOnException)<br>
4) pause()<br>
5) resume()<br><br>

1, 2, 4, and 5 are managed by the TaskExecutor and likely will not ever be used directly. 3, setRemoveOnException may be useful; if your Task experiences an exception during 
execution, and the Task is part of the queue, do you want it to be removed from the queue? This is obviously irrelevant if the Task wasn't queue using the runTask() method. For queued tasks by default if the Task experiences an exception it will not be automatically removed from the queue.
<br><br>
The TaskExecutor has a queue for you to bulk execute Tasks. You can use the addToQueue() and removeFromQueue() methods, followed by the executeQueue() method. You cannot add to the queue while it's executing; such a circumstance will throw an illegalStateException. You can directly execute Tasks using the runTask() method, but Tasks 
executed as such will not have their callback updated in onResume, thus will not accommodate configurationChanges gracefully compared to queued Tasks.