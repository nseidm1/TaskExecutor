TaskExecutor
===================

<b>TaskExecutorActivity</b><br>
The included abstract TaskExecutorActivity class makes for easy use. Simply extend the class and utilize the protected mTaskExecutor reference to execute Tasks. 
TaskExecutorActivity has two abstract methods, allowTaskFiness() and specifyServiceMode(). 
Tasks can be finessed to maintain a callback to the currently visible Activity, and 
the the Service has two MODEs, CALLBACK_INCONSIDERATE and CALLBACK_DEPENDENT. 
The Service mode is intented to define how the Service 
treaks Tasks restored from disk when the service goes through onCreate after being killed by the system.

TaskExecutorActivity has a couple of interfaces used by the Service. TasksRestoredCallback, TaskCompletedCallback, 
and TaskExecutorReferenceCallback. The names really say it all. TaskRestoredCallback informs the current activity 
that Tasks have been restored from disk. TaskCompletedCallback is a hard callback gracefully managed for each Task 
to post back to the ui thread in the currently visible Activity. It doesn't matter if you start a new Activity, the callback 
in all Tasks will always be the current visibile Activity. TaskExecutorReferenceCallback is how the service provides 
a reference of TaskExecutor to the current Activity.

Tasks Are runnables with additional helper methods to facilitate management by the TaskExecutor facility. 
Task is an abstract class you'll have to extend, and cannot be anonymous. Anonymous Tasks cannot be restored from 
there persisted state on disk. If Tasks are implemented properly, and the Service is killed by the system, all executed 
Tasks will be restored from a persisted state on disk; and depending on the Service MODE can even continue execution 
automatically or wait for an Activity to launch in turn providing a hard callback for the Task to post it's completion 
results on the ui thread.

The heart of Task is the abstract method task() that you'll override to define what your Task does. If designed as a 
static inner class do not reference stuff outside of the Task's scope, keep everything within the Task itself to gracefully 
accommodate restoration from disk, the launching of new activities, and just for general good coding practice. 

The TaskExecutor is managed by a Service, but accessible in a unique way. TaskExecutorActivity makes a static request to the Service requesting a callback with a reference to 
the TaskExecutor. So it's a service, or is it a singleton, who know but it just feels right! :-)

Tasks have 8 public methods:<br>
1) setCompleteCallback(TaskCompletedCallback completeCallback)<br>
2) setTaskExecutor(TaskExecutor taskExecutor)<br>
3) pause()<br>
4) resume()<br>
5) setBundle(Bundle bundle)<br>
6) getBundle()<br>
7) setTag(String TAG)<br>
80) getTag()<br>
The only method you'll likely want to use is setBundle(), all others are managed by the TaskExecutor and likely do not need to be used directly.

<b>Tips</b><br>
1) The TaskExecutor has a queue for you to bulk execute Tasks. You use the addToQueue() and removeFromQueue() methods, 
followed by the executeQueue() method. <br>
2) If you add items to the queue after calling executeQueue(), you'll have to call executeQueue() at a future time. <br>
4) TaskExecutor has the findTaskByTag(String TAG) method, so you can keep a local List of Task TAGs, and locate them if they are in the queue<br>
5) Tasks also take a bundle. I highly encourage proper design, your Task should be designed to perform a discrete operation on the Bundle's data.
<br><br>

COPYRIGHT

Copyright 2012 Noah Seidman

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.