Thread groups

In a network server program, one thread waits for and accepts requests from client programs to execute, for example, database transactions or complex calculations. The thread usually creates a new thread to handle the request. Depending on the request volume, many different threads might be simultaneously present, complicating thread management. To simplify thread management, programs organize their threads with thread groups—java.lang.ThreadGroup objects that group related threads' Thread (and Thread subclass) objects. For example, your program can use ThreadGroup to group all printing threads into one group.

Note: To keep the discussion simple, I refer to thread groups as if they organize threads. In reality, thread groups organize Thread (and Thread subclass) objects associated with threads.

Java requires every thread and every thread group—save the root thread group, system—to join some other thread group. That arrangement leads to a hierarchical thread-group structure, which the figure below illustrates in an application context.

Figure 1. An application's hierarchical thread-group structure begins with a main thread group just below the system thread group

At the top of the figure's structure is the system thread group. The JVM-created system group organizes JVM threads that deal with object finalization and other system tasks, and serves as the root thread group of an application's hierarchical thread-group structure. Just below system is the JVM-created main thread group, which is system's subthread group (subgroup, for short). main contains at least one thread—the JVM-created main thread that executes byte-code instructions in the main() method.

Below the main group reside the subgroup 1 and subgroup 2 subgroups, application-created subgroups (which the figure's application creates). Furthermore, subgroup 1 groups three application-created threads: thread 1, thread 2, and thread 3. In contrast, subgroup 2 groups one application-created thread: my thread.

Now that you know the basics, let's start creating thread groups.

Create thread groups and associate threads with those groups

The ThreadGroup class's SDK documentation reveals two constructors: ThreadGroup(String name) and ThreadGroup(ThreadGroup parent, String name). Both constructors create a thread group and give it a name, as the name parameter specifies. The constructors differ in their choice of what thread group serves as parent to the newly created thread group. Each thread group, except system, must have a parent thread group. For ThreadGroup(String name), the parent is the thread group of the thread that calls ThreadGroup(String name). As an example, if the main thread calls ThreadGroup(String name), the newly created thread group has the main thread's group as its parent—main. For ThreadGroup(ThreadGroup parent, String name), the parent is the group that parent references. The following code shows how to use these constructors to create a pair of thread groups:

public static void main (String [] args)
{
   ThreadGroup tg1 = new ThreadGroup ("A");
   ThreadGroup tg2 = new ThreadGroup (tg1, "B");
}
In the code above, the main thread creates two thread groups: A and B. First, the main thread creates A by calling ThreadGroup(String name). The tg1-referenced thread group's parent is main because main is the main thread's thread group. Second, the main thread creates B by calling ThreadGroup(ThreadGroup parent, String name). The tg2-referenced thread group's parent is A because tg1's reference passes as an argument to ThreadGroup (tg1, "B") and A associates with tg1.

Tip: Once you no longer need a hierarchy of ThreadGroup objects, call ThreadGroup's void destroy() method via a reference to the ThreadGroup object at the top of that hierarchy. If the top ThreadGroup object and all subgroup objects lack thread objects, destroy() prepares those thread group objects for garbage collection. Otherwise, destroy() throws an IllegalThreadStateException object. However, until you nullify the reference to the top ThreadGroup object (assuming a field variable contains that reference), the garbage collector cannot collect that object. Referencing the top object, you can determine if a previous call was made to the destroy() method by calling ThreadGroup's boolean isDestroyed() method. That method returns true if the thread group hierarchy was destroyed.

By themselves, thread groups are useless. To be of any use, they must group threads. You group threads into thread groups by passing ThreadGroup references to appropriate Thread constructors:

ThreadGroup tg = new ThreadGroup ("subgroup 2");
Thread t = new Thread (tg, "my thread");
The code above first creates a subgroup 2 group with main as the parent group. (I assume the main thread executes the code.) The code next creates a my thread Thread object in the subgroup 2 group.

Now, let's create an application that produces our figure's hierarchical thread-group structure:

Listing 1. ThreadGroupDemo.java

ThreadGroupDemo creates the appropriate thread group and thread objects to mirror what you see in the figure above. To prove that the subgroup 1 and subgroup 2 groups are main's only subgroups, ThreadGroupDemo does the following:

Retrieves a reference to the main thread's ThreadGroup object by calling Thread's static currentThread() method (which returns a reference to the main thread's Thread object) followed by Thread's ThreadGroup getThreadGroup() method.
Calls ThreadGroup's int activeGroupCount() method on the just-returned ThreadGroup reference to return an estimate of active groups within the main thread's thread group.
Calls ThreadGroup's String getName () method to return the main thread's thread group name.
Calls ThreadGroup's void list () method to print on the standard output device details on the main thread's thread group and all subgroups.
When run, ThreadGroupDemo displays the following output:

Active thread groups in main thread group: 2
java.lang.ThreadGroup[name=main,maxpri=10]
    Thread[main,5,main]
    Thread[Thread-0,5,main]
    java.lang.ThreadGroup[name=subgroup 1,maxpri=10]
        Thread[thread 1,5,subgroup 1]
        Thread[thread 2,5,subgroup 1]
        Thread[thread 3,5,subgroup 1]
    java.lang.ThreadGroup[name=subgroup 2,maxpri=10]
        Thread[my thread,5,subgroup 2]
Output that begins with Thread results from list()'s internal calls to Thread's toString() method, an output format I described in Part 1. Along with that output, you see output beginning with java.lang.ThreadGroup. That output identifies the thread group's name followed by its maximum priority.

Priority and thread groups

A thread group's maximum priority is the highest priority any of its threads can attain. Consider the aforementioned network server program. Within that program, a thread waits for and accepts requests from client programs. Before doing that, the wait-for/accept-request thread might first create a thread group with a maximum priority just below that thread's priority. Later, when a request arrives, the wait-for/accept-request thread creates a new thread to respond to the client request and adds the new thread to the previously created thread group. The new thread's priority automatically lowers to the thread group's maximum. That way, the wait-for/accept-request thread responds more often to requests because it runs more often.

Java assigns a maximum priority to each thread group. When you create a group, Java obtains that priority from its parent group. Use ThreadGroup's void setMaxPriority(int priority) method to subsequently set the maximum priority. Any threads that you add to the group after setting its maximum priority cannot have a priority that exceeds the maximum. Any thread with a higher priority automatically lowers when it joins the thread group. However, if you use setMaxPriority(int priority) to lower a group's maximum priority, all threads added to the group prior to that method call keep their original priorities. For example, if you add a priority 8 thread to a maximum priority 9 group, and then lower that group's maximum priority to 7, the priority 8 thread remains at priority 8. At any time, you can determine a thread group's maximum priority by calling ThreadGroup's int getMaxPriority() method. To demonstrate priority and thread groups, I wrote MaxPriorityDemo:

Listing 2. MaxPriorityDemo.java

When run, MaxPriorityDemo produces the following output:

tg maximum priority = 10
t1 priority = 5
t1 priority after setPriority() = 6
tg maximum priority after setMaxPriority() = 4
t1 priority after setMaxPriority() = 6
t2 priority = 4
t2 priority after setPriority() = 4
Thread group A (which tg references) starts with the highest priority (10) as its maximum. Thread X, whose Thread object t1 references, joins the group and receives 5 as its priority. We change that thread's priority to 6, which succeeds because 6 is less than 10. Subsequently, we call setMaxPriority(int priority) to reduce the group's maximum priority to 4. Although thread X remains at priority 6, a newly-added Y thread receives 4 as its priority. Finally, an attempt to increase thread Y's priority to 5 fails, because 5 is greater than 4.

Note: setMaxPriority(int priority) automatically adjusts the maximum priority of a thread group's subgroups.

In addition to using thread groups to limit thread priority, you can accomplish other tasks by calling various ThreadGroup methods that apply to each group's thread. Methods include void suspend(), void resume(), void stop(), and void interrupt(). Because Sun Microsystems has deprecated the first three methods (they are unsafe), we examine only interrupt().

Interrupt a thread group

ThreadGroup's interrupt() method allows a thread to interrupt a specific thread group's threads and subgroups. This technique would prove appropriate in the following scenario: Your application's main thread creates multiple threads that each perform a unit of work. Because all threads must complete their respective work units before any thread can examine the results, each thread waits after completing its work unit. The main thread monitors the work state. Once all other threads are waiting, the main thread calls interrupt() to interrupt the other threads' waits. Then those threads can examine and process the results. Listing 3 demonstrates thread group interruption:

Listing 3. InterruptThreadGroup.java

The main thread creates and starts threads A and B before sleeping for 2,000 milliseconds to give A and B a chance to wait. Upon waking, the main thread assumes A and B are waiting, and executes Thread.currentThread ().getThreadGroup ().interrupt (); to interrupt those threads. Because A and B execute in a synchronized context, one thread will throw an InterruptedException object and finish its processing before the other thread does the same. When run, InterruptThreadGroup produces the following output (for one invocation):

A about to wait.
B about to wait.
A interrupted.
A terminating.
B interrupted.
B terminating.

A is interrupted and terminates before B is interrupted and terminates.

You'll occasionally want to enumerate all threads or subgroups that comprise a thread group. The next section explores that activity.

Enumerate threads and subgroups

Part 1 introduced you to Thread's activeCount() and enumerate(Thread [] thdarray) methods. activeCount() calls ThreadGroup's int activeCount() method via the current thread's group reference to return an estimate of the active threads in the current thread's group and subgroups. enumerate(Thread [] thdarray) calls ThreadGroup's int enumerate(Thread [] thdarray) method via the current thread's group reference. enumerate(Thread [] thdarray) is just one of four enumeration methods in ThreadGroup:

int enumerate(Thread [] thdarray) copies into thdarray references to every active thread in the current thread group and all subgroups.
int enumerate(Thread [] thdarray, boolean recurse) copies into thdarray references to every active thread in the current thread group only if recurse is false. Otherwise, this method includes active threads from subgroups.
int enumerate(ThreadGroup [] tgarray) copies into tgarray references to every active subgroup in the current thread group.
int enumerate(ThreadGroup [] tgarray, boolean recurse) copies into tgarray references to every active subgroup in the current thread group only if recurse is false. Otherwise, this method includes all active subgroups of active subgroups, active subgroups of active subgroups of active subgroups, and so on.
You can use ThreadGroup's activeCount and enumerate(Thread [] thdarray) methods to enumerate all program threads. First, you find the system thread group. Then you call ThreadGroup's activeCount() method to retrieve an active thread count for array-sizing purposes. Next, you call ThreadGroup's enumerate(Thread [] thdarray) method to populate that array with Thread references, as Listing 4 demonstrates:

Listing 4. EnumThreads.java

When run, EnumThreads produces the following output (on my platform):

Thread[Reference Handler,10,system] true
Thread[Finalizer,8,system] true
Thread[Signal Dispatcher,10,system] true
Thread[CompileThread0,10,system] true
Thread[main,5,main] false
Apart from a main thread, all other threads belong to the system thread group.

Tip: You can easily determine a thread group's parent group by calling ThreadGroup's ThreadGroup getParent() method. For all thread groups, save system, this method returns a nonnull reference. For system, this method returns null. You can also find out if a thread group is the parent, grandparent, and so forth of another thread group by calling ThreadGroup's boolean parentOf(ThreadGroup tg) method. That method returns true if a thread, whose reference you use to call parentOf(ThreadGroup tg), is a parent (or other ancestor) of the group that tg references—or is the same group as the tg-referenced group. Otherwise, the method returns false.

Volatility

Volatility, that is, changeability, describes the situation where one thread changes a shared field variable's value and another thread sees that change. You expect other threads to always see a shared field variable's value, but that is not necessarily the case. For performance reasons, Java does not require a JVM implementation to read a value from or write a value to a shared field variable in main memory, or object heap memory. Instead, the JVM might read a shared field variable's value from a processor register or cache, collectively known as working memory. Similarly, the JVM might write a shared field variable's value to a processor register or cache. That capability affects how threads share field variables, as you will see.

Suppose a program creates a shared integer-field variable x whose initial value in main memory is 10. This program starts two threads; one thread writes to x, and the other reads x's value. Finally, this program runs on a JVM implementation that assigns each thread its own private working memory, meaning each thread has its own private copy of x. When the writing thread writes 6 to x, the writing thread only updates its private working-memory copy of x; the thread does not update the main-memory copy. Also, when the reading thread reads from x, the returned value comes from the reading thread's private copy. Hence, the reading thread returns 10 (because a shared field variable's private working-memory copies initialize to values taken from the main-memory counterpart), not 6. As a result, one thread is unaware of another's change to a shared field variable.

A thread's inability to observe another thread's modification to a shared field variable can cause serious problems. For example, last month's YieldDemo application contained a pair of shared field variables: finished and sum. For JVMs that support separate working memory for each thread, the main and main-created YieldDemo threads can have their own copies of finished and sum. As a result, the main thread's execution of finished = true; would not affect the YieldDemo thread's copy of that variable—and the YieldDemo thread would never terminate.

When you ran YieldDemo, you probably discovered that program eventually terminated. That implies your JVM implementation reads/writes main memory instead of working memory. But if you found that the program did not terminate, you probably encountered a situation where the main thread set its working memory copy of finished to true, not the equivalent main-memory copy. Also, the YieldDemo thread read its own working-memory copy of finished, and never saw true.

To fix YieldDemo's visibility problem (on those JVMs that support working memory), include Java's volatile keyword in the finished and sum declarations: static volatile boolean finished = false; and static volatile int sum = 0;. The volatile keyword ensures that when a thread writes to a volatile shared field variable, the JVM modifies the main-memory copy, not the thread's working-memory copy. Similarly, the JVM ensures that a thread always reads from the main-memory copy.

Caution: The volatile and final keywords cannot appear together in a shared field variable declaration. Any attempt to include both keywords forces the compiler to report an error.

The visibility problem does not occur when threads use synchronization to access shared field variables. When a thread acquires a lock, the thread's working-memory copies of shared field variables reload from their main-memory counterparts. Similarly, when a thread releases a lock, the working-memory copies flush back to the main-memory shared field variables. For example, in last month's ProdCons2 application, the producer and consumer threads read from/wrote to the writeable shared field variable's main-memory copy because all access to that shared field variable happened within synchronized contexts. As a result, synchronization allows threads to communicate via shared field variables.

Tip: To ensure that a read/write operation (outside a synchronized context) on either a long-integer shared field variable or a double-precision floating-point shared field variable succeeds, prefix the shared field variable's declaration with keyword volatile.

New developers sometimes think volatility replaces synchronization. Although volatility, through keyword volatile, lets you assign values to long-integer or double-precision floating-point shared field variables outside a synchronized context, volatility cannot replace synchronization. Synchronization lets you group several operations into an indivisible unit, which you cannot do with volatility. However, because volatility is faster than synchronization, use volatility in situations where multiple threads must communicate via a single shared field variable.

Thread-local variables

Sun's Java 2 Platform, Standard Edition (J2SE) SDK 1.2 introduced the java.lang.ThreadLocal class, which developers use to create thread-local variables—ThreadLocal objects that store values on a per-thread basis. Each ThreadLocal object maintains a separate value (such as a user ID) for each thread that accesses the object. Furthermore, a thread manipulates its own value and can't access other values in the same thread-local variable.

ThreadLocal has three methods:

Object get (): Returns the calling thread's value from the thread-local variable. Because this method is thread-safe, you can call get() from outside a synchronized context.
Object initialValue (): Returns the calling thread's initial value from the thread-local variable. Each thread's first call to either get() or set(Object value) results in an indirect call to initialValue() to initialize that thread's value in the thread-local variable. Because ThreadLocal's default implementation of initialValue() returns null, you must subclass ThreadLocal and override this method to return a nonnull initial value.
void set (Object value): Sets the current thread's value in the thread-local variable to value. Use this method to replace the value that initialValue() returns.

Listing 5 shows you how to use ThreadLocal:

Listing 5. ThreadLocalDemo1.java

ThreadLocalDemo1 creates a thread-local variable. That variable associates a unique serial number with each thread that accesses the thread-local variable by calling tl.get (). When a thread first calls tl.get (), ThreadLocal's get() method calls the overridden initialValue() method in the anonymous ThreadLocal subclass. The following output results from one program invocation:

A 100
A 100
A 100
A 100
A 100
A 100
A 100
A 100
A 100
A 100
B 101
B 101
B 101
B 101
B 101
B 101
B 101
B 101
B 101
B 101
C 102
C 102
C 102
C 102
C 102
C 102
C 102
C 102
C 102
C 102

The output associates each thread name (A, B, or C) with a unique serial number. If you run this program a second time, you might see a different serial number associate with a thread name. Though the number differs, it always associates with a single thread name.

Note: To allow multiple threads access to the same ThreadLocal object, ThreadLocalDemo1 uses the static keyword. Without that keyword, each thread accesses its own ThreadLocal object, with each object containing only a value for one thread, not a separate value for each thread. Because thread-local variables store values on a per-thread basis, failing to use static in a thread-local variable declaration serves little purpose.

An alternative to overriding ThreadLocal's initialValue() method is calling that class's set(Object value) method to provide an initial value:

Listing 6. ThreadLocalDemo2.java

ThreadLocalDemo2 is nearly identical to ThreadLocalDemo1. However, instead of overriding initialValue() to establish each thread's initial value to a unique serial number, ThreadLocalDemo2 uses a tl.set ("" + sernum++); method call. If you run this program, your output will be more or less identical (on an invocation-by-invocation basis) to ThreadLocalDemo1's output.

Before leaving this section, we must consider one other topic: inheritance and thread-local variables. When a thread creates another thread, the creating thread is the parent thread and the created thread is the child thread. For example, the main thread that executes the main() method's byte-code instructions is the parent of all threads that those instructions create. A child cannot inherit a parent's thread-local values that the parent thread establishes via the ThreadLocal class. However, a parent can use java.lang.InheritableThreadLocal (which extends ThreadLocal) to pass the values of inheritable thread-local variables to a child, as Listing 7 demonstrates:

Listing 7. InheritableThreadLocalDemo.java

InheritableThreadLocalDemo creates an inheritable thread-local variable via the InheritableThreadLocal class and a thread-local variable via the ThreadLocal class. Furthermore, the main thread calls each variable's set(Object value) method to establish an initial value before creating and starting two child threads. When each child calls get() to retrieve that value, only the inheritable thread-local variable's value returns, as the following output (from one invocation) demonstrates:

parent thread thread-local value passed to child thread
null
parent thread thread-local value passed to child thread
null

The output shows each child thread printing parent thread thread-local value passed to child thread, which is the inheritable thread-local variable's value. However, null prints as the ThreadLocal variable's value.

Tip: Override InheritableThreadLocal's childValue(Object parentvalue) method to make the child's inheritable thread-local value a function of the parent's inheritable thread-local value.

Timers

Programs occasionally need a timer mechanism to execute code either once or periodically, and either at some specified time or after a time interval. Before Sun released J2SE 1.3, a developer either created a custom timer mechanism or relied on another's mechanism. Incompatible timer mechanisms led to difficult-to-maintain source code. Recognizing a need to standardize timer mechanisms, Sun introduced two timer classes in SDK 1.3: java.util.Timer and java.util.TimerTask.

Note: Sun also introduced the javax.swing.Timer class in the 1.3 SDK. I don't present that class here because that discussion requires Swing knowledge. After I introduce you to Swing in a future article, I'll explore Swing's Timer class.

According to the SDK, use a Timer object to schedule tasks—TimerTask and subclass objects—for execution. That execution relies on a thread associated with the Timer object. To create a Timer object, call either the Timer() or Timer(boolean isDaemon) constructor. The constructors differ in the threads they create to execute tasks: Timer() creates a nondaemon thread, whereas Timer(boolean isDaemon) creates a daemon thread, when isDaemon contains true. The following code demonstrates both constructors creating Timer objects:

Timer t1 = new Timer (); // Create a nondaemon thread to execute all tasks
Timer T2 = new Timer (true); // Create a daemon thread to execute all tasks

Once you create a Timer object, you need a TimerTask to execute. Do that by subclassing TimerTask and overriding TimerTask's run() method. (TimerTask implements Runnable, which specifies the run() method.) The following code fragment demonstrates:

class MyTask extends TimerTask
{
   public void run ()
   {
      System.out.println ("MyTask task is running.");
   }
}
Now that you have a Timer object and a TimerTask subclass, to schedule a TimerTask object for one-time or repeated execution, call one of Timer's four schedule() methods:

void schedule(TimerTask task, Date time): Schedules task for one-time execution at the specified time.
void schedule(TimerTask task, Date firstTime, long interval): Schedules task for repeated execution at the specified firstTime and at interval millisecond intervals following firstTime. This execution is known as fixed-delay execution because each subsequent task execution occurs relative to the previous task execution's actual execution time. Furthermore, if an execution delays because of garbage collection or some other background activity, all subsequent executions also delay.
void schedule(TimerTask task, long delay): Schedules task for one-time execution after delay milliseconds pass.
void schedule(TimerTask task, long delay, long interval): Schedules task for repeated execution after delay milliseconds pass and at interval millisecond intervals following firstTime. This method employs fixed-delay execution.
The following code fragment, which assumes a t1-referenced Timer object, creates a MyTask object and schedules that object using the fourth method in the above list for repeated execution (every second), following an initial delay of zero milliseconds:

t1.schedule (new MyTask (), 0, 1000);
Every second (that is, 1,000 milliseconds), the Timer's thread executes the MyTask's run() method. For a more useful example of task execution, check out Listing 8:

Listing 8. Clock1.java