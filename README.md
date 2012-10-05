TaskExecutorService
===================

The included abstract FragmentActivity class makes for easy use. Simply extend the class and utilize the protected mTaskExecutor reference to execute Tasks. Tasks 
Are runnables with additional helper methods to facilitate management by the executor. Tasks take a TaskCompletedCallback which the abstract class implements. You will 
receive callbacks to this method when executed Tasks are completed.
<br><br>
By default executed Tasks are paused when the activity is paused. If a Task is currently being executed it will continue, but the runnable will be blocked prior to the callback. 
This is the default behavior to facilitate onConfigurationChange events and activity destruction; in onResume all Tasks will 
have their callback reset, and the queue will be unblocked.
<br><br>
The TaskExecutor is designed to be static to accommodate activity destruction in onConfigurationChange events gracefully. Callbacks will be done in the ui thread.