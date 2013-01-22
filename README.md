TaskExecutor
===================

<img src="http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Highway_401_by_401-DVP.jpg/320px-Highway_401_by_401-DVP.jpg"/>

<b>What problem does the TaskExecutor solve?</b></br><br>
Too many projects I've seen where AsyncTask, and threading in general is done without consideration. I've even seen anonymous 
AyncTask implementations :( The TaskExecutor consolidates ALL asynchronous 
activity into a single ExecutorService accessible in ALL Activities application wide via a uniquely queried Service. 
No more anonymous threading, no more starting a thread in one Activity, and having to think about what happens when it's 
completes if the user opened a new Activity. 

Do you worry about null pointers in onProgressUpdate()?<br>
What happens if a user launches a new Activity while your thread is running?<br>
What happens if the user presses the home button during a long running operation?

The TaskExecutor manages everything for you, updates your ui callbacks to have a valid reference when a new Activity is launched, 
and will even queue completion results if no Activity is available. The TaskExecutor is a super custom, rock solid, implementation 
of a robust, consolidated, and centralized asynchronous Task execution framework. Tasks are persisted to disk, 
accommodate configurationChanges, new Activity creation, and even survive process termination. With many options, 
your Tasks are almost guaranteed to execute.

The most important power of the TaskExecutor is the consolidation of all asynchronous activity. Control, organization, and consolidation is 
the only way to take an app to a higher level. With the TaskExecutor you'll always know where your Tasks are executing, and exactly 
where the ui callbacks will be.

<b>Quick Info</b><br>
1) Tasks are persisted to disk, and the Service has several configurations on how to handle restored Tasks considering process termination.<br>
2) Tasks can remain, or be removed from the queue, on success/exception. This is useful for network communication, if their is an exception
the Task can just be re-executed.<br>
3) The service has an auto execute mode that is designed specifically for #2.<br>
4) Tasks mirror the two main ui callbacks of AsyncTask. Progress update, and post execute. The callbacks are gracefully managed 
by the TaskExecutor to always be in the curently visible activity. Both callbacks take a bundle, and onTaskComplete will 
provide the exception if one occured. Strategic data can be added to the bundles definining what events should occur in the ui. For example 
progress updates can get a reference to a DialogFragment and trigger message updates, or even increment a horizontal progress bar.<br>
5) If a Task completes and all Activities are closed the results, by default, will be queued and delivered to the next Activity that launches. 
Maybe the results aren't important, which is the beauty of passing bundles around; just define your onTaskComplete method to process strategically 
bundled info however you'd like.<br>

<b>TaskActivity</b><br>
The included abstract TaskActivity class makes for easy use. Simply extend the class and utilize the protected 
mTaskExecutor reference to execute Tasks. 

TaskActivity has four abstract methods, specifyServiceMode(), specifyAutoexecMode(), autoExecuteRestoredTasks(), and taskExecutorReferenceAvailable(). 

The Service has two MODEs, CALLBACK_INCONSIDERATE and CALLBACK_DEPENDENT which 
are pertinent when Tasks are restored from disk 
after the Service is killed by the OS; oh did I mention that Tasks survive process termintion :-) 
When the Service is in CALLBACK_INCONSIDERATE mode 
the Service will auto execute restored Tasks without a valid ui callback. In CALLBACK_DEPENDENT mode the Service 
wait for the the next activity to assign a callback, and if you set autoExecuteRestoredTasks to true the restored 
Tasks will be auto executed.

TaskActivity has a couple of interfaces used to do all the dirty work. TasksRestoredCallback, TaskCompletedCallback, TaskUpdateCallback, 
and ExecutorReferenceCallback. The names really say it all. TaskCompletedCallback, and TaskUpdateCallback aren't defined in the abstract TaskActivity, and are for you to define in 
your implementation.

<b>Tasks</b><br>
Tasks are runnables with additional helper methods to facilitate management by the TaskExecutor framework. 
Task is an abstract class you'll have to extend, cannot be anonymous, and needs an empty public constructor. 
Anonymous Tasks cannot be restored from their persisted state on disk. If Tasks are implemented properly, and the Service is killed by the system, all executed 
Tasks can be restored from a persisted state on disk; and depending on the Service MODE can even continue execution 
automatically or wait for an Activity to launch to provide a ui callback.

The heart of a Task is the abstract method task() that you'll override to define what your Task does. The same pattern as defining 
a Runnable. If designed as a static inner class do not reference stuff outside of the Task's scope, keep everything 
within the Task itself to gracefully accommodate restoration from disk, always possible null references, and just 
for general good coding practice. 

The TaskExecutor is managed by a Service, but accessible in a unique way. TaskActivity makes a static request to the Service requesting a callback with a reference to 
the TaskExecutor. So it's a service with a dash of singleton feel, but with a predictable defined lifecycle :-)

<pre>
Tasks have 10 public methods:
1) setTaskExecutor(TaskExecutor taskExecutor)
2) setMainBundle(Bundle bundle)
3) getMainBundle()
4) setTag(String TAG)
5) getTag()
6) setRemoveFromQueueOnSuccess(boolean removeFromQueueOnSuccess)
7) The getter for 6.
8) setRemoveFromQueueOnException(boolean removeFromQueueOnException)
9) The getter for 8.
10) setCacheResultsIfNoActivity(boolean setCacheResultsIfNoActivity)
</pre>

<b>Tips</b><br>
1) The TaskExecutor has a queue for you to bulk execute Tasks. You use the addToQueue() and removeFromQueue() methods, 
followed by the executeQueue() method.<br>
2) If you add items to the queue after calling executeQueue(), you'll have to call executeQueue() at a future time.<br>
4) TaskExecutor has the findTaskByTag(String TAG) method, so you can keep a local List of Task TAGs, and locate them as needed<br>
5) Tasks also take a bundle. I highly encourage proper design, your Task should be designed to perform a discrete operation on the Bundle's data. The main 
bundle is returned in the onTaskComplete callback, and you can define any bundle you'd like to pass to the onTaskUpdate callback.
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
