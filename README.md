TaskExecutor
===================

<img src="http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Highway_401_by_401-DVP.jpg/320px-Highway_401_by_401-DVP.jpg"/>

<b>What problem does the TaskExecutor solve?</b></br><br>
When you typically implement an AsyncTask the ui callback is going to be in that particular instance of the Activity, 
huh? So your doing something in the background, and if it takes a fair amount of time the user may go to a new 
Activity, what happen in the onPostExecute()? No matter what the ui callback of a Task will always be in the current 
Activity. TaskExecutor is an AsyncTask on sterroids.

Too many projects I've seen where AsyncTask, and threading is done without consideration. I've even seen anonymous 
AyncTask implementations that make me throw up a little. The TaskExecutor consolidates ALL asynchronous 
activity into a single Executor service accessible in ALL Activities application wide. You can even set a 
custom ThreadPoolExecutor if you want to define exactly how your Tasks are asynchronously executed. No more 
anonymous threading, no more starting a thread in one Activity, and having to think about what happens when it's 
completes if the user opened a new Activity. 

<br>Intro</b><br>
The TaskExecutor is a super custom, rock solid, awesome implementation of a robust, consolidated, and centralized asynchronous Task execution framework. Tasks are persisted to disk, accommodate configurationChanges, new Activity creation, and even survive process termination. With many options, your Tasks are almost guaranteed to execute, and post back directly to the current activity via a hard callback. 

<b>Quick Info</b><br>
1) Tasks are persisted to disk, and the Service has several configurations on how to handle restored Tasks considering process termination.<br>
2) Tasks can remain, or be removed from the queue, on success/exception. This is useful for network communication, if their is an exception
the Task can just be re-executed.<br>
3) The service has an auto execute mode that is designed specifically for #2.<br>
4) Tasks mirror the two main ui callbacks of AsyncTask. Progress update, and post execute. The callbacks are gracefully managed 
by the TaskExecutor to always be in the curently visible activity. Both callbacks take a bundle, and the post execute will 
provide the exception if one occured. Strategic data can be added to the bundles definining what events should occur in the ui. For example 
progress updates can get a reference the a DialogFragment and trigger message updates, or even increment a horizontal progress bar.<br>
5) If a Task completes and all Activities are closed the results, by default, will be queued and delivered to the next Activity that launches. 
Maybe the results aren't important, which is the beauty of passing bundles around; just define your onTaskComplete method to process strategically 
bundled info however you'd like.<br>
6) The most important power of the TaskExecutor is the consolidation of all asynchronous activity. Control, organization, and consolidation is 
the only way to take an app to a higher level. With the TaskExecutor you'll always know where your Tasks are executing, and exactly 
where the ui callbacks will be.<br>

<b>TaskActivity</b><br>
The included abstract TaskActivity class makes for easy use. Simply extend the class and utilize the protected 
mTaskExecutor reference to execute Tasks. 

TaskActivity has three abstract methods, specifyServiceMode(), specifyAutoexecMode(), and 
autoExecuteRestoredTasks(). 

The the Service has two MODEs, SERVICE_MODE_CALLBACK_INCONSIDERATE and SERVICE_MODE_CALLBACK_DEPENDENT which 
are pertinent when Tasks are restored from disk 
after the Service is killed by the OS; oh did I mention that Tasks survive process termintion :-) 
When the Service is in SERVICE_MODE_CALLBACK_INCONSIDERATE mode 
the Service will auto execute restored Tasks without a valid ui callback. In SERVICE_MODE_CALLBACK_DEPENDENT mode the Service 
wait for the the next activity to assign a callback, and if you set autoExecuteRestoredTasks to true the restored 
Tasks will be auto executed.

TaskExecutorActivity has a couple of interfaces used by the Service. TasksRestoredCallback, TaskCompletedCallback, TaskUpdateCallback, 
and ExecutorReferenceCallback. The names really say it all. TasksRestoredCallback informs the current activity 
that Tasks have been restored from disk. TaskCompletedCallback, and TaskUpdateCallback are hard callbacks gracefully managed for each Task 
to post back to the ui thread in the currently visible Activity. It doesn't matter if you start a new Activity, the callback 
for all Tasks will always be the current visibile Activity. ExecutorReferenceCallback is how the service provides 
a reference of TaskExecutor to the current Activity.

Tasks are runnables with additional helper methods to facilitate management by the TaskExecutor facility. 
Task is an abstract class you'll have to extend, and cannot be anonymous. Anonymous Tasks cannot be restored from 
their persisted state on disk. If Tasks are implemented properly, and the Service is killed by the system, all executed 
Tasks can be restored from a persisted state on disk; and depending on the Service MODE can even continue execution 
automatically or wait for an Activity to launch to provide a ui callback.

The heart of Task is the abstract method task() that you'll override to define what your Task does. The same pattern as defining 
a Runnable. If designed as a static inner class do not reference stuff outside of the Task's scope, keep everything 
within the Task itself to gracefully accommodate restoration from disk, the launching of new activities, and just 
for general good coding practice. 

The TaskExecutor is managed by a Service, but accessible in a unique way. TaskActivity makes a static request to the Service requesting a callback with a reference to 
the TaskExecutor. So it's a service, or is it a singleton, who know but it looks cool! :-)

<pre>
Tasks have 9 public methods:
1) setTaskExecutor(TaskExecutor taskExecutor)
2) setMainBundle(Bundle bundle)
3) getMainBundle()
4) setTag(String TAG)
5) getTag()
6) setRemoveFromQueueOnSuccess(boolean shouldRemoveFromQueueOnSuccess)
7) The getter for 6.
8) setRemoveFromQueueOnException(boolean shouldRemoveFromQueueOnException)
9) The getter for 8.
</pre>

The only method you may want to use is setMainBundle(), all others are managed by the TaskExecutor and likely do not need to be used directly. 
6, and 8 may come in handy, and are self explanatory in the method name.

<b>Tips</b><br>
1) The TaskExecutor has a queue for you to bulk execute Tasks. You use the addToQueue() and removeFromQueue() methods, 
followed by the executeQueue() method.<br>
2) If you add items to the queue after calling executeQueue(), you'll have to call executeQueue() at a future time.<br>
4) TaskExecutor has the findTaskByTag(String TAG) method, so you can keep a local List of Task TAGs, and locate them is needed<br>
5) Tasks also take a bundle. I highly encourage proper design, your Task should be designed to perform a discrete operation on the Bundle's data.
<br><br>

<pre><code>Copyright 2012 Noah Seidman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</code></pre>
