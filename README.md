TaskExecutor
===================

The included abstract TaskExecutorActivity class makes for easy use. Simply extend the class and utilize the protected mTaskExecutor reference to execute Tasks. Tasks 
Are runnables with additional helper methods to facilitate management by the TaskExecutor facility. Tasks take a TaskCompletedCallback which the abstract TaskExecutorActivity implements. You will 
receive callbacks to this interface when executed Tasks are completed.
<br><br>
By default queue execution is paused when the activity is paused, but if a Task is currently being executed it will continue, but the runnable will be blocked prior to the callback. 
This is the default behavior to facilitate configurationChange events and activity destruction; in onResume all Tasks will 
have their callback reset, and the queue will be unblocked. If you expect your activity to die for a period of time it may be best to call setPermitCallbackIfPaused() prior to onPause(). This will prevent 
the queue from blocking when the activity is paused, and will allow the queue to continue executing.

<b>Tasks</b><br>
Tasks are extended Runnables, and instead of overriding run you'll override the task method. The Task constructor takes a reference to the TaskCompletedCallback which the abstract activity implements.
<br><br>
Tasks have 7 public methods:<br>
1) setCompleteCallback(TaskCompletedCallback completeCallback)<br>
2) setTaskExecutor(TaskExecutor taskExecutor)<br>
3) setRemoveOnException(Boolean removeOnException)<br>
4) pause()<br>
5) resume()<br>
6) setBundle(Bundle bundle)<br>
7) getBundle()<br><br>
<b>Tips</b><br>
1, 2, 4, and 5 are managed by the TaskExecutor and likely will not ever be used directly.
<br><br>
The TaskExecutor has a queue for you to bulk execute Tasks. You can use the addToQueue() and removeFromQueue() methods, followed by the executeQueue() method. You cannot add to the queue while it's executing; such a circumstance will throw an illegalStateException. You can directly execute Tasks using the runTask() method, but Tasks 
executed as such will not have their callback updated in onResume, thus will not accommodate configurationChanges gracefully compared to queued Tasks. It's recommended to use the queue facility and not the runTask feature. setRemoveOnException() may be useful; if your Task experiences an exception during 
execution, and the Task is part of the queue, do you want it to be removed from the queue? This is obviously irrelevant if the Task wasn't queue using the runTask() method. For queued tasks, by default, if the Task experiences an exception it will not be automatically removed from the queue, so you can call executeQueue() again to attempt re-execution. Tasks can 
have a TAG set, and the TaskExecutor has the findTaskByTag(String TAG) method. Tasks also take a bundle, and the Task is returned in the onCompleteCallback to provide access to the bundle for at minimum identification purposes.
<br><br>

COPYRIGHT

Copyright 2012 Noah Seidman

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.