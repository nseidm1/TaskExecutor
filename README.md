TaskExecutor
===================

<b>Task Executor Activity</b><br>
The included abstract TaskExecutorActivity class makes for easy use. Simply extend the class and utilize the protected mTaskExecutor reference to execute Tasks. Tasks 
Are runnables with additional helper methods to facilitate management by the TaskExecutor facility. Tasks take a TaskCompletedCallback which the abstract TaskExecutorActivity implements. You will 
receive callbacks to this interface when executed Tasks are completed. By default queue execution is paused when the activity is paused, but if a Task is currently being executed it will continue, and the runnable will be blocked prior to the callback. 
This is the default behavior to facilitate configurationChange events and activity destruction; in onResume all Tasks will 
have their callback reset, and the queue will be unblocked. It you want your Tasks to continue execution, and not be tied to the activity lifecycle use of TaskExecutorService is encouraged.

<b>Task Executor Service</b><br>
The included TaskExecutorService is a powerful facility. Use requestExecutorReference(Context context, TaskExecutorReferenceCallback serviceReferenceCallback), and a reference to the service will be provided
in the TaskExecutorReferenceCallback which your activities can implement. The service has the same functionality as the TaskExecutorActivity, but does not need to be paused and can continue execution without 
an activity in the foreground. Keep in mind that if your activity is destroyed so will the TaskCompletedCallback; please define your Task considering this possible circumstance. The service will automatically save unexecuted Tasks to disk in onDestroy, and will restore them
in the onCreate. The service is designed to be STICKY so your Tasks have a high assurance of being executed even if your activity is destroyed or your service is paused/stopped by the system.

TODO for version 1.1<br>
The queue will be written to disk upon modifiction. This will accommodate process termintion. Currently the queue is written to disk 
only in the onDestroy of the service, which is not entirely effective, if effective at all.<br><br>

<b>Tasks</b><br>
Tasks are extended Runnables, and instead of overriding run you'll override the task method. The constructor takes a Bundle to encourange proper design. Your Task should be designed 
to perform a concrete action on the Bundle. It's recommended not to reference items outside of the Tasks's scope. Again, the task method is designed 
to perform a discrete operation on the Bundle.
<br><br>
Tasks have 10 public methods:<br>
1) setCompleteCallback(TaskCompletedCallback completeCallback)<br>
2) setTaskExecutor(TaskExecutor taskExecutor)<br>
3) setRemoveOnException(Boolean removeOnException)<br>
4) setRemoveOnSuccess(Boolen removeOnSuccess)<br>
5) pause()<br>
6) resume()<br>
7) setBundle(Bundle bundle)<br>
8) getBundle()<br>
9) setTag(String TAG)<br>
10) getTag()<br><br>
<b>Tips</b><br>
1, 2, 4, and 5 are managed by the TaskExecutor and likely will not ever be used directly.
<br><br>
1) The TaskExecutor has a queue for you to bulk execute Tasks. You can use the addToQueue() and removeFromQueue() methods, 
followed by the executeQueue() method. <br>
2) If you add items to the queue after calling executeQueue(), you'll have to call executeQueue() at a future time. You'll probably want to check the queue for existing items, as you may 
want to double check your not executing items that are already been executed depending on how you executed the Task (boolean removeOnException, boolean removeOnSuccess). <br>
3) setRemoveOnException() may be useful; if your Task experiences an exception 
during execution, and the Task is part of the queue, do you want it to be removed from the queue?<br>
4) TaskExecutor has the findTaskByTag(String TAG) method.<br>
5) Tasks also take a bundle in the constructor. This is to encourage proper design as your Task should be designed to perform a discrete operation on the Bundle's data.
<br><br>

COPYRIGHT

Copyright 2012 Noah Seidman

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.