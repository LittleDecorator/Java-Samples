Thread scheduling

In an idealized world, all program threads would have their own processors on which to run. Until the time comes when computers have thousands or millions of processors, threads often must share one or more processors. Either the JVM or the underlying platform's operating system deciphers how to share the processor resource among threads—a task known as thread scheduling. That portion of the JVM or operating system that performs thread scheduling is a thread scheduler.

Note: To simplify my thread scheduling discussion, I focus on thread scheduling in the context of a single processor. You can extrapolate this discussion to multiple processors; I leave that task to you.

Remember two important points about thread scheduling:

Java does not force a VM to schedule threads in a specific manner or contain a thread scheduler. That implies platform-dependent thread scheduling. Therefore, you must exercise care when writing a Java program whose behavior depends on how threads are scheduled and must operate consistently across different platforms.
Fortunately, when writing Java programs, you need to think about how Java schedules threads only when at least one of your program's threads heavily uses the processor for long time periods and intermediate results of that thread's execution prove important. For example, an applet contains a thread that dynamically creates an image. Periodically, you want the painting thread to draw that image's current contents so the user can see how the image progresses. To ensure that the calculation thread does not monopolize the processor, consider thread scheduling.
Examine a program that creates two processor-intensive threads:

Listing 1. SchedDemo.java

SchedDemo creates two threads that each calculate the value of pi (five times) and print each result. Depending upon how your JVM implementation schedules threads, you might see output resembling the following:

CalcThread A: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
According to the above output, the thread scheduler shares the processor between both threads. However, you could see output similar to this:

CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread A: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
CalcThread B: 3.1415726535897894
The above output shows the thread scheduler favoring one thread over another. The two outputs above illustrate two general categories of thread schedulers: green and native. I'll explore their behavioral differences in upcoming sections. While discussing each category, I refer to thread states, of which there are four:

Initial state: A program has created a thread's thread object, but the thread does not yet exist because the thread object's start() method has not yet been called.
Runnable state: This is a thread's default state. After the call to start() completes, a thread becomes runnable whether or not that thread is running, that is, using the processor. Although many threads might be runnable, only one currently runs. Thread schedulers determine which runnable thread to assign to the processor.
Blocked state: When a thread executes the sleep(), wait(), or join() methods, when a thread attempts to read data not yet available from a network, and when a thread waits to acquire a lock, that thread is in the blocked state: it is neither running nor in a position to run. (You can probably think of other times when a thread would wait for something to happen.) When a blocked thread unblocks, that thread moves to the runnable state.
Terminating state: Once execution leaves a thread's run() method, that thread is in the terminating state. In other words, the thread ceases to exist.

How does the thread scheduler choose which runnable thread to run? I begin answering that question while discussing green thread scheduling. I finish the answer while discussing native thread scheduling.

Green thread scheduling

Not all operating systems, the ancient Microsoft Windows 3.1 perating system, for example, support threads. For such systems, Sun Microsystems can design a JVM that divides its sole thread of execution into multiple threads. The JVM (not the underlying platform's operating system) supplies the threading logic and contains the thread scheduler. JVM threads are green threads, or user threads.

A JVM's thread scheduler schedules green threads according to priority—a thread's relative importance, which you express as an integer from a well-defined range of values. Typically, a JVM's thread scheduler chooses the highest-priority thread and allows that thread to run until it either terminates or blocks. At that time, the thread scheduler chooses a thread of the next highest priority. That thread (usually) runs until it terminates or blocks. If, while a thread runs, a thread of higher priority unblocks (perhaps the higher-priority thread's sleep time expired), the thread scheduler preempts, or interrupts, the lower-priority thread and assigns the unblocked higher-priority thread to the processor.

Note: A runnable thread with the highest priority will not always run. Here's the Java Language Specification's take on priority:

Every thread has a priority. When there is competition for processing resources, threads with higher priority are generally executed in preference to threads with lower priority. Such preference is not, however, a guarantee that the highest priority thread will always be running, and thread priorities cannot be used to reliably implement mutual exclusion.
That admission says much about the implementation of green thread JVMs. Those JVMs cannot afford to let threads block because that would tie up the JVM's sole thread of execution. Therefore, when a thread must block, such as when that thread is reading data slow to arrive from a file, the JVM might stop the thread's execution and use a polling mechanism to determine when data arrives. While the thread remains stopped, the JVM's thread scheduler might schedule a lower-priority thread to run. Suppose data arrives while the lower-priority thread is running. Although the higher-priority thread should run as soon as data arrives, that doesn't happen until the JVM next polls the operating system and discovers the arrival. Hence, the lower-priority thread runs even though the higher-priority thread should run. ou need to worry about this situation only when you need real-time behavior from Java. But then Java is not a real-time operating system, so why worry?


To understand which runnable green thread becomes the currently running green thread, consider the following. Suppose your application consists of three threads: the main thread that runs the main() method, a calculation thread, and a thread that reads keyboard input. When there is no keyboard input, the reading thread blocks. Assume the reading thread has the highest priority and the calculation thread has the lowest priority. (For simplicity's sake, also assume that no other internal JVM threads are available.) Figure 1 illustrates the execution of these three threads.

At time T0, the main thread starts running. At time T1, the main thread starts the calculation thread. Because the calculation thread has a lower priority than the main thread, the calculation thread waits for the processor. At time T2, the main thread starts the reading thread. Because the reading thread has a higher priority than the main thread, the main thread waits for the processor while the reading thread runs. At time T3, the reading thread blocks and the main thread runs. At time T4, the reading thread unblocks and runs; the main thread waits. Finally, at time T5, the reading thread blocks and the main thread runs. This alternation in execution between the reading and main threads continues as long as the program runs. The calculation thread never runs because it has the lowest priority and thus starves for processor attention, a situation known as processor starvation.

We can alter this scenario by giving the calculation thread the same priority as the main thread. Figure 2 shows the result, beginning with time T2. (Prior to T2, Figure 2 is identical to Figure 1.)

At time T2, the reading thread runs while the main and calculation threads wait for the processor. At time T3, the reading thread blocks and the calculation thread runs, because the main thread ran just before the reading thread. At time T4, the reading thread unblocks and runs; the main and calculation threads wait. At time T5, the reading thread blocks and the main thread runs, because the calculation thread ran just before the reading thread. This alternation in execution between the main and calculation threads continues as long as the program runs and depends on the

Figure 2. Thread-scheduling diagram for equal priority main and calculation threads, and a different priority reading thread

higher-priority thread running and blocking.

We must consider one last item in green thread scheduling. What happens when a lower-priority thread holds a lock that a higher-priority thread requires? The higher-priority thread blocks because it cannot get the lock, which implies that the higher-priority thread effectively has the same priority as the lower-priority thread. For example, a priority 6 thread attempts to acquire a lock that a priority 3 thread holds. Because the priority 6 thread must wait until it can acquire the lock, the priority 6 thread ends up with a 3 priority—a phenomenon known as priority inversion.

Priority inversion can greatly delay the execution of a higher-priority thread. For example, suppose you have three threads with priorities of 3, 4, and 9. Priority 3 thread is running and the other threads are blocked. Assume that the priority 3 thread grabs a lock, and the priority 4 thread unblocks. The priority 4 thread becomes the currently running thread. Because the priority 9 thread requires the lock, it continues to wait until the priority 3 thread releases the lock. However, the priority 3 thread cannot release the lock until the priority 4 thread blocks or terminates. As a result, the priority 9 thread delays its execution.

A JVM's thread scheduler usually solves the priority inversion problem through priority inheritance: The thread scheduler silently raises the priority of the thread holding the lock when a higher-priority thread requests the lock. As a result, both the thread holding the lock and the thread waiting for the lock temporarily have equal priorities. Using the previous example, the priority 3 thread (holding the lock) would temporarily become a priority 9 thread as soon as the priority 9 thread attempts to acquire the lock and is blocked. As a result, the priority 9 thread (holding the lock) would become the currently running thread (even when the priority 4 thread unblocks). The priority 9 thread would finish its execution and release the lock, allowing the waiting priority 9 thread to acquire the lock and continue execution. The priority 4 thread would lack the chance to become the currently running thread. Once the thread with its silently raised priority releases the lock, the thread scheduler restores the thread's priority to its original priority. Therefore, the thread scheduler would restore the priority 9 thread previously holding the lock to priority 3, once it releases the lock. Thus, priority inheritance ensures that a lower-priority thread holding a lock is not preempted by a thread whose priority exceeds the lock-holding thread's priority but is less than the priority of the thread waiting for the lock to release.

Native thread scheduling

Most JVMs rely on the underlying operating system (such as Linux or Microsoft Windows XP) to provide a thread scheduler. When an operating system handles thread scheduling, the threads are native threads. As with green thread scheduling, priority proves important to native thread scheduling: higher-priority threads typically preempt lower-priority threads. But native thread schedulers often introduce an additional detail: time-slicing.

Note: Some green thread schedulers also support time-slicing. And many native thread schedulers support priority inheritance. As a result, green thread schedulers and native thread schedulers normally differ only in their thread scheduler's source: JVM or operating system.

Native thread schedulers typically introduce time-slicing to prevent processor starvation of equal-priority threads. The idea is to give each equal-priority thread the same amount of time, known as a quantum. A timer tracks each quantum's remaining time and alerts the thread scheduler when the quantum expires. The thread scheduler then schedules another equal-priority thread to run, unless a higher-priority thread unblocks.


Time-slicing complicates the writing of those platform-independent multithreaded programs that depend on consistent thread scheduling, because not all thread schedulers implement time-slicing. Without time-slicing, an equal-priority runnable thread will keep running (assuming it is the currently running thread) until that thread terminates, blocks, or is replaced by a higher-priority thread. Thus, the thread scheduler fails to give all equal-priority runnable threads the chance to run. Though complicated, time-slicing does not prevent you from writing platform-independent multithreaded programs. The setPriority(int priority) and yield() methods influence thread scheduling so a program behaves fairly consistently (as far as thread scheduling is concerned) across platforms.

Note: To prevent lower-priority threads from starving, some thread schedulers, such as Windows schedulers, give temporary priority boosts to threads that have not run in a long time. When the thread runs, that priority boost decays. Thread schedulers still give higher-priority threads preference over lower-priority threads, but at least all threads receive a chance to run.

Schedule with the setPriority(int priority) method

Enough theory! Let's learn how to influence thread scheduling at the source code level. One way is to use Thread's void setPriority(int priority); method. When called, setPriority(int priority) sets the priority of a thread associated with the specified thread object (as in thd.setPriority (7);), to priority. If priority is not within the range of priorities that Thread's MIN_PRIORITY and MAX_PRIORITY constants specify, setPriority(int priority) throws an IllegalArgumentException object.

Note: If you call setPriority(int priority) with a priority value that exceeds the maximum allowed priority for the respective thread's thread group, this method silently lowers the priority value to match the thread group's maximum priority. (I'll discuss thread groups next month.)

When you must determine a thread's current priority, call Thread's int getPriority() method, via that thread's thread object. The getPriority() method returns a value between MIN_PRIORITY (1) and MAX_PRIORITY (10). One of those values might be 5—the value that assigns to the NORM_PRIORITY constant, which represents a thread's default priority.

The setPriority(int priority) method proves useful in preventing processor starvation. For example, suppose your program consists of a thread that blocks and a calculation thread that doesn't block. By assigning a higher priority to the thread that blocks, you ensure the calculation thread will not starve the blocking thread. Because the blocking thread periodically blocks, the calculation thread will not starve. Of course, we assume the thread scheduler does not support time-slicing. If the thread scheduler does supports time-slicing, you will probably see no difference between calls to setPriority(int priority) and no calls to that method, depending on what the affected threads are doing. However, you will at least ensure that your code ports across thread schedulers. To demonstrate setPriority(int priority), I wrote PriorityDemo:

Listing 2. PriorityDemo.java


PriorityDemo has a blocking thread and a calculating thread in addition to the main thread. Suppose you ran this program on a platform where the thread scheduler did not support time-slicing. What would happen? Consider two scenarios:

Assume no bt.setPriority (Thread.NORM_PRIORITY + 1); method call: The main thread runs until it sleeps. At that point, assume the thread scheduler starts the blocking thread. That thread runs until it calls System.in.read(), which causes the blocking thread to block. The thread scheduler then assigns the calculating thread to the processor (assuming the main thread has not yet unblocked from its sleep). Because the blocking, main, and calculating threads all have the same priority, the calculating thread continues to run in an infinite loop.
Assume bt.setPriority (Thread.NORM_PRIORITY + 1); method call: The blocking thread gets the processor once it unblocks. Then assume that the thread scheduler arbitrarily chooses either the calculating thread or the main thread (assuming the main thread has unblocked from its sleep) when the blocking thread blocks upon its next call to System.in.read(). As a result, the program should eventually end. If the thread scheduler always picks the calculating thread over the main thread, consider boosting the main thread's priority to ensure eventual termination.
If you run PriorityDemo with time-slicing, you have the following two scenarios:

Assume no bt.setPriority (Thread.NORM_PRIORITY + 1); method call: Time-slicing ensures that all equal-priority threads have a chance to run. The program eventually terminates.
Assume bt.setPriority (Thread.NORM_PRIORITY + 1); method call: The blocking thread will run more often because of its higher priority. But because it blocks periodically, the blocking thread does not cause significant disruption to the calculation and main threads. The program eventually terminates.
Schedule with the yield() method

Many developers prefer the alternative to the setPriority(int priority) method, Thread's static void yield();, because of its simplicity. When the currently running thread calls Thread.yield ();, the thread scheduler keeps the currently running thread in the runnable state, but (usually) picks another thread of equal priority to be the currently running thread, unless a higher-priority thread has just been made runnable, in which case the higher-priority thread becomes the currently running thread. If you have no higher-priority thread and no other equal-priority threads, the thread scheduler immediately reschedules the thread calling yield() as the currently running thread. Furthermore, when the thread scheduler picks an equal-priority thread, the picked thread might be the thread that called yield()—which means that yield() accomplishes nothing except delay. This behavior typically happens under a time-slicing thread scheduler. Listing 3 demonstrates the yield() method:

Listing 3. YieldDemo.java

From a logical perspective, YieldDemo's main thread starts a new thread (of the same NORM_PRIORITY priority) that repeatedly outputs the value of instance field sum until the value of instance field finished is true. After starting that thread, the main thread enters a loop that repeatedly increments sum's value. If no arguments pass to YieldDemo on the command line, the main thread calls Thread.yield (); after each increment. Otherwise, no call is made to that method. Once the loop ends, the main thread assigns true to finished, so the other thread will terminate. After that, the main thread terminates.

Now that you know what YieldDemo should accomplish, what kind of behavior can you expect? That answer depends on whether the thread scheduler uses time-slicing and whether calls are made to yield(). We have four scenarios to consider:

No time-slicing and no yield() calls: The main thread runs to completion. The thread scheduler won't schedule the output thread once the main thread exits. Therefore, you see no output.
No time-slicing and yield() calls: After the first yield() call, the output thread runs forever because finished contains false. You should see the same sum value printed repeatedly in an endless loop (because the main thread does not run and increment sum). To counteract this problem, the output thread should also call yield() during each while loop iteration.
Time-slicing and no yield() calls: Both threads have approximately equal amounts of time to run. However, you will probably see very few lines of output because each System.out.println ("sum =" + sum); method call occupies a greater portion of a quantum than a sum++; statement. (Many processor cycles are required to send output to the standard output device, while (relatively) few processor cycles are necessary for incrementing an integer variable.) Because the main thread accomplishes more work by the end of a quantum than the output thread and because that activity brings the program closer to the end, you observe fewer lines of output.
Time-slicing and yield() calls: Because the main thread yields each time it increments sum, the main thread completes less work during a quantum. Because of that, and because the output thread receives additional quantums, you see many more output lines.
Note: Should you call setPriority(int priority) or yield()? Both methods affect threads similarly. However, setPriority(int priority) offers flexibility, whereas yield() offers simplicity. Also, yield() might immediately reschedule the yielding thread, which accomplishes nothing. I prefer setPriority(int priority), but you must make your own choice.

The wait/notify mechanism

As you learned last month, each object's associated lock and waiting area allow the JVM to synchronize access to critical code sections. For example: When thread X tries to acquire a lock before entering a synchronized context guarding a critical code section from concurrent thread access, and thread Y is executing within that context (and holding the lock), the JVM places X in a waiting area. When Y exits the synchronized context (and releases the lock), the JVM removes X from the waiting area, assigns the lock to X, and allows that thread to enter the synchronized context. In addition to its use in synchronization, the waiting area serves a second purpose: it is part of the wait/notify mechanism, the mechanism that coordinates multiple threads' activities.

The idea behind the wait/notify mechanism is this: A thread forces itself to wait for some kind of condition, a prerequisite for continued execution, to exist before it continues. The waiting thread assumes that some other thread will create that condition and then notify the waiting thread to continue execution. Typically, a thread examines the contents of a condition variable—a Boolean variable that determines whether a thread will wait—to confirm that a condition does not exist. If a condition does not exist, the thread waits in an object's waiting area. Later, another thread will set the condition by modifying the condition variable's contents and then notifying the waiting thread that the condition now exists and the waiting thread can continue execution.

Tip:Think of a condition as the reason one thread waits and another thread notifies the waiting thread.

To support the wait/notify mechanism, Object declares the void wait(); method (to force a thread to wait) and the void notify(); method (to notify a waiting thread that it can continue execution). Because every object inherits Object's methods, wait() and notify() are available to all objects. Both methods share a common feature: they are synchronized. A thread must call wait() or notify() from within a synchronized context because of a race condition inherent to the wait/notify mechanism. Here is how that race condition works:

Thread A tests a condition and discovers it must wait.
Thread B sets the condition and calls notify() to inform A to resume execution. Because A is not yet waiting, nothing happens.
Thread A waits, by calling wait().
Because of the prior notify() call, A waits indefinitely.
To solve the race condition, Java requires a thread to enter a synchronized context before it calls either wait() or notify(). Furthermore, the thread that calls wait() (the waiting thread) and the thread that calls notify() (the notification thread) must compete for the same lock. Either thread must call wait() or notify() via the same object on which they enter their synchronized contexts because wait() tightly integrates with the lock. Prior to waiting, a thread executing wait() releases the lock, which allows the notification thread to enter its synchronized context to set the condition and notify the waiting thread. Once notification arrives, the JVM wakens the waiting thread, which then tries to reacquire the lock. Upon successfully reacquiring the lock, the previously waiting thread returns from wait(). Confused? The following code fragment offers clarification:

// Condition variable initialized to false to indicate condition has not occurred.
boolean conditionVar = false;
// Object whose lock threads synchronize on.
Object lockObject = new Object ();
// Thread A waiting for condition to occur...
synchronized (lockObject)
{
   while (!conditionVar)
      try
      {
         lockObject.wait ();
      }
      catch (InterruptedException e) {}
}
// ... some other method
// Thread B notifying waiting thread that condition has now occurred...
synchronized (lockObject)
{
   conditionVar = true;
   lockObject.notify ();
}


The code fragment introduces condition variable conditionVar, which threads A and B use to test and set a condition, and lock variable lockObject, which both threads use for synchronization purposes. The condition variable initializes to false because the condition does not exist when the code starts execution. When A needs to wait for a condition, it enters a synchronized context (provided B is not in its synchronized context). Once inside its context, A executes a while loop statement whose Boolean expression tests conditionVar's value and waits (if the value is false) by calling wait(). Notice that lockObject appears as part of synchronized (lockObject) and lockObject.wait ();—that is no coincidence. From inside wait(), A releases the lock associated with the object on which the call to wait() is made—the object associated with lockObject in the lockObject.wait (); method call. This allows B to enter its synchronized (lockObject) context, set conditionVar to true, and call lockObject.notify (); to notify A that the condition now exists. Upon receiving notification, A attempts to reacquire its lock. That does not occur until B leaves its synchronized context. Once A reacquires its lock, it returns from wait() and retests the condition variable. If this variable's value is true, A leaves its synchronized context.

Caution: If a call is made to wait() or notify() from outside a synchronized context, either call results in an IllegalMonitorStateException.

Apply wait/notify to the producer-consumer relationship

To demonstrate wait/notify's practicality, I introduce you to the producer-consumer relationship, which is common among multithreaded programs where two or more threads must coordinate their activities. The producer-consumer relationship demonstrates coordination between a pair of threads: a producer thread (producer) and a consumer thread (consumer). The producer produces some item that a consumer consumes. For example, a producer reads items from a file and passes those items to a consumer for processing. The producer cannot produce an item if no room is available for storing that item because the consumer has not finished consuming its item(s). Also, a consumer cannot consume an item that does not exist. Those restrictions prevent a producer from producing items that a consumer never receives for consumption, and prevents a consumer from attempting to consume more items than are available. Listing 4 shows the architecture of a producer-/consumer-oriented program:

Listing 4. ProdCons1.java

ProdCons1 creates producer and consumer threads. The producer passes uppercase letters individually to the consumer by calling s.setSharedChar (ch);. Once the producer finishes, that thread terminates. The consumer receives uppercase characters, from within a loop, by calling s.getSharedChar (). The loop's duration depends on that method's return value. When Z returns, the loop ends, and, thus, the producer informs the consumer when to finish. To make the code more representative of real-world programs, each thread sleeps for a random time period (up to four seconds) before either producing or consuming an item.

Because the code contains no race conditions, the synchronized keyword is absent. Everything seems fine: the consumer consumes every character that the producer produces. In reality, some problems exist, which the following partial output from one invocation of this program shows:

consumed by consumer.
A produced by producer.
B produced by producer.
B consumed by consumer.
C produced by producer.
C consumed by consumer.
D produced by producer.
D consumed by consumer.
E produced by producer.
F produced by producer.
F consumed by consumer.
The first output line, consumed by consumer., shows the consumer trying to consume a nonexisting uppercase letter. The output also shows the producer producing a letter (A) that the consumer does not consume. Those problems do not result from lack of synchronization. Instead, the problems result from lack of coordination between the producer and the consumer. The producer should execute first, produce a single item, and then wait until it receives notification that the consumer has consumed the item. The consumer should wait until the producer produces an item. If both threads coordinate their activities in that manner, the aforementioned problems will disappear. Listing 5 demonstrates that coordination, which the wait/notify mechanism initiates:

Listing 5. ProdCons2.java

When you run ProdCons2, you should see the following output (abbreviated for brevity):

A produced by producer.
A consumed by consumer.
B produced by producer.
B consumed by consumer.
C produced by producer.
C consumed by consumer.
D produced by producer.
D consumed by consumer.
E produced by producer.
E consumed by consumer.
F produced by producer.
F consumed by consumer.
G produced by producer.
G consumed by consumer.

The problems disappeared. The producer always executes before the consumer and never produces an item before the consumer has a chance to consume it. To produce this output, ProdCons2 uses the wait/notify mechanism.

The wait/notify mechanism appears in the Shared class. Specifically, wait() and notify() appear in Shared's setSharedChar(char c) and getSharedChar() methods. Shared also introduces a writeable instance field, the condition variable that works with wait() and notify() to coordinate the execution of the producer and consumer. Here is how that coordination works, assuming the consumer executes first:

The consumer executes s.getSharedChar ().
Within that synchronized method, the consumer calls wait() (because writeable contains true). The consumer waits until it receives notification.
At some point, the producer calls s.setSharedChar (ch);.
When the producer enters that synchronized method (possible because the consumer released the lock inside the wait() method just before waiting), the producer discovers writeable's value as true and does not call wait().
The producer saves the character, sets writeable to false (so the producer must wait if the consumer has not consumed the character by the time the producer next invokes setSharedChar(char c)), and calls notify() to waken the consumer (assuming the consumer is waiting).
The producer exits setSharedChar(char c).
The consumer wakens, sets writeable to true (so the consumer must wait if the producer has not produced a character by the time the consumer next invokes getSharedChar()), notifies the producer to awaken that thread (assuming the producer is waiting), and returns the shared character.
Note: To write more reliable programs that use wait/notify, think about what conditions exist in your program. For example, what conditions exist in ProdCons2? Although ProdCons2 contains only one condition variable, there are two conditions. The first condition is the producer waiting for the consumer to consume a character and the consumer notifying the producer when it consumes the character. The second condition represents the consumer waiting for the producer to produce a character and the producer notifying the consumer when it produces the character.

The rest of the family

In addition to wait() and notify(), three other methods make up the wait/notify mechanism's method family: void wait(long millis);, void wait(long millis, int nanos);, and void notifyAll();.

The overloaded wait(long millis) and wait(long millis, int nanos) methods allow you to limit how long a thread must wait. wait(long millis) limits the waiting period to millis milliseconds, and wait(long millis, int nanos) limits the waiting period to a combination of millis milliseconds and nanos nanoseconds. As with the no-argument wait() method, code must call these methods from within a synchronized context.

You use wait(long millis) and wait(long millis, int nanos) in situations where a thread must know when notification arrives. For example, suppose your program contains a thread that connects to a server. That thread might be willing to wait up to 45 seconds to connect. If the connection does not occur in that time, the thread must attempt to contact a backup server. By executing wait (45000);, the thread ensures it will wait no more than 45 seconds.

notifyAll() wakens all waiting threads associated with a given lock—unlike the notify() method, which awakens only a single thread. Although all threads wake up, they must still reacquire the object lock. The JVM selects one of those threads to acquire the lock and allows that thread to run. When that thread releases the lock, the JVM automatically selects another thread to acquire the lock. That continues until all threads have run. Examine Listing 6 for an example of notifyAll():

Listing 6. WaitNotifyAllDemo.java

WaitNotifyAllDemo's main thread creates three MyThread objects and assigns names A, B, and C to the associated threads, which subsequently start. The main thread then sleeps for three seconds to give the newly created threads time to wait. After waking up, the main thread calls notifyAll() to awaken those threads. One by one, each thread leaves its synchronized statement and run() method, then terminates.

Tip: This article demonstrates notify() in the ProdCons2 program because only one thread waits for a condition to occur. In your programs, where more than one thread might simultaneously wait for the same condition to occur, consider using notifyAll(). That way, no waiting thread waits indefinitely.

Thread interruption

One thread can interrupt another thread that is either waiting or sleeping by calling Thread's void interrupt(); method. In response, the waiting/sleeping thread resumes execution by creating an object from InterruptedException and throwing that object from the wait() or sleep() methods.

Note: Because the join() methods call sleep() (directly or indirectly), the join() methods can also throw InterruptedException objects.

When a call is made to interrupt(), that method either allows a waiting/sleeping thread to resume execution via a thrown exception object or sets a Boolean flag to true (somewhere in the appropriate thread object) to indicate that an executing thread has been interrupted. The method sets the flag only if the thread is neither waiting nor sleeping. An executing thread can determine the Boolean flag's state by calling one of the following Thread methods: static boolean interrupted(); (for the current thread) or boolean isInterrupted(); (for a specific thread). These methods feature two differences:

Because interrupted() is a static method, you do not need a thread object before you call it. For example: System.out.println (Thread.interrupted ()); // Display Boolean flag value for current thread. In contrast, because isInterrupted() is a nonstatic method, you need a thread object before you call that method.
The interrupted() method clears the Boolean flag to false, whereas the isInterrupted() method does not modify the Boolean flag.
In a nutshell, interrupt() sets the Boolean flag in a thread object and interrupted()/isInterrupted() returns that flag's state. How do we use this capability? Examine Listing 7's ThreadInterruptionDemo source code:

Listing 7. ThreadInterruptionDemo.java

ThreadInterruptionDemo starts a pair of threads: A and B. A sleeps for a random amount of time (up to 10 seconds) before calling interrupt() on B's thread object. B continually checks for interruption by calling its thread object's isInterrupted() method. As long as that method returns false, B executes the statements within the while loop statement. Those statements cause B to sleep for a random amount of time (up to 10 milliseconds), print variable count's value, and increment that value.

When A calls interrupt(), B is either sleeping or not sleeping. If B is sleeping, B wakes up and throws an InterruptedException object from the sleep() method. The catch clause then executes, and B calls interrupt() on its thread object to set B's Boolean flag to true. (That flag clears to false when the exception object is thrown.) The next call to isInterrupted() causes execution to leave the while loop statement because isInterrupted() returns true. The result: B terminates. If B is not sleeping, consider two scenarios. First, B has just called isInterrupted() and is about to call sleep() when A calls interrupt(). B's call to sleep() results in that method immediately throwing an InterruptedException object. This scenario is then identical to when B was sleeping: B eventually terminates. Second, B is executing System.out.println (getName () + " " + count++); when A calls interrupt(). B completes that method call and calls isInterrupted(). That method returns true, B breaks out of the while loop statement, and B terminates.

Review

This article continued to explore Java's threading capabilities by focusing on thread scheduling, the wait/notify mechanism, and thread interruption. You learned that thread scheduling involves either the JVM or the underlying platform's operating system deciphering how to share the processor resource among threads. Furthermore, you learned that the wait/notify mechanism makes it possible for threads to coordinate their executions—to achieve ordered execution, as in the producer-consumer relationship. Finally, you learned that thread interruption allows one thread to prematurely awaken a sleeping or waiting thread.

This article's material proves important for three reasons. First, thread scheduling helps you write platform-independent programs where thread scheduling is an issue. Second, situations (such as the producer-consumer relationship) arise where you must order thread execution. The wait/notify mechanism helps you accomplish that task. Third, you can interrupt threads when your program must terminate even though other threads are waiting or sleeping.
