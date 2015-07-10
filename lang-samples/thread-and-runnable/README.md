# Java-Samples
Simple Java basic samples

Lang-samples
1. Threads and runnables

What is a thread?

Conceptually, the notion of a thread is not difficult to grasp: it's an independent path of execution through program code.
When multiple threads execute, one thread's path through the same code usually differs from the others.
For example, suppose one thread executes the byte code equivalent of an if-else statement's if part, while another thread executes the byte code equivalent of the else part.
How does the JVM keep track of each thread's execution? The JVM gives each thread its own method-call stack.
In addition to tracking the current byte code instruction, the method-call stack tracks local variables, parameters the JVM passes to a method, and the method's return value.

When multiple threads execute byte-code instruction sequences in the same program, that action is known as multithreading. Multithreading benefits a program in various ways:

Multithreaded GUI (graphical user interface)-based programs remain responsive to users while performing other tasks, such as repaginating or printing a document.
Threaded programs typically finish faster than their nonthreaded counterparts. This is especially true of threads running on a multiprocessor machine, where each thread has its own processor.
Java accomplishes multithreading through its java.lang.Thread class. Each Thread object describes a single thread of execution.
That execution occurs in Thread's run() method. Because the default run() method does nothing, you must subclass Thread and override run() to accomplish useful work.
For a taste of threads and multithreading in the context of Thread, examine "SimpleThreadSample".

It's source code contain classes SimpleThreadSample and MyThread. Class SimpleThreadSample drives the application by creating a MyThread object, starting a thread that associates with that object,
and executing some code to print a table of squares. In contrast, MyThread overrides Thread's run() method to print (on the standard output stream) a right-angle triangle composed of asterisk characters.

//Thread scheduling and the JVM
//Most (if not all) JVM implementations use the underlying platform's threading capabilities.
//Because those capabilities are platform-specific, the order of your multithreaded programs' output might differ from the order of someone else's output. That difference results from scheduling, a topic I explore later in this series.

When you type java SimpleThreadSample to run the application, the JVM creates a starting thread of execution, which executes the main() method.
By executing mt.start ();, the starting thread tells the JVM to create a second thread of execution that executes the byte code instructions comprising the MyThread object's run() method.
When the start() method returns, the starting thread executes its for loop to print a table of squares, while the new thread executes the run() method to print the right-angle triangle.

What does the output look like? Run SimpleThreadSample to find out. You will notice each thread's output tends to intersperse with the other's output. That results because both threads send their output to the same standard output stream.

##The Thread class

To grow proficient at writing multithreaded code, you must first understand the various methods that make up the Thread class. This section explores many of those methods.
Specifically, you learn about methods for starting threads, naming threads, putting threads to sleep, determining whether a thread is alive, joining one thread to another thread,
and enumerating all active threads in the current thread's thread group and subgroups. I also discuss Thread's debugging aids and user threads versus daemon threads.

I'll present the remainder of Thread's methods in subsequent articles, with the exception of Sun's deprecated methods.

//Deprecated methods
//Sun has deprecated a variety of Thread methods, such as suspend() and resume(), because they can lock up your programs or damage objects.
//As a result, you should not call them in your code. Consult the SDK documentation for workarounds to those methods. I do not cover deprecated methods in this series.

Constructing threads

Thread has eight constructors. The simplest are:

Thread(), which creates a Thread object with a default name
Thread(String name), which creates a Thread object with a name that the name argument specifies
The next simplest constructors are Thread(Runnable target) and Thread(Runnable target, String name). Apart from the Runnable parameters, those constructors are identical to the aforementioned constructors.
The difference: The Runnable parameters identify objects outside Thread that provide the run() methods. (You learn about Runnable later in this article.)
The final four constructors resemble Thread(String name), Thread(Runnable target), and Thread(Runnable target, String name); however, the final constructors also include a ThreadGroup argument for organizational purposes.

One of the final four constructors, Thread(ThreadGroup group, Runnable target, String name, long stackSize), is interesting in that it lets you specify the desired size of the thread's method-call stack.
Being able to specify that size proves helpful in programs with methods that utilize recursion—an execution technique whereby a method repeatedly calls itself—to elegantly solve certain problems.
By explicitly setting the stack size, you can sometimes prevent StackOverflowErrors. However, too large a size can result in OutOfMemoryErrors.
Also, Sun regards the method-call stack's size as platform-dependent. Depending on the platform, the method-call stack's size might change.
Therefore, think carefully about the ramifications to your program before writing code that calls Thread(ThreadGroup group, Runnable target, String name, long stackSize).

##Start your vehicles

Threads resemble vehicles: they move programs from start to finish. Thread and Thread subclass objects are not threads.
Instead, they describe a thread's attributes, such as its name, and contain code (via a run() method) that the thread executes.
When the time comes for a new thread to execute run(), another thread calls the Thread's or its subclass object's start() method. For example, to start a second thread, the application's starting thread—which executes main()—calls start().
In response, the JVM's thread-handling code works with the platform to ensure the thread properly initializes and calls a Thread's or its subclass object's run() method.

Once start() completes, multiple threads execute. Because we tend to think in a linear fashion, we often find it difficult to understand the concurrent (simultaneous) activity that occurs when two or more threads are running.
Therefore, you should examine a chart that shows where a thread is executing (its position) versus time.

 There are several significant time periods:

 1.The starting thread's initialization
 2.The moment that thread begins to execute main()
 3.The moment that thread begins to execute start()
 4.The moment start() creates a new thread and returns to main()
 5.The new thread's initialization
 6.The moment the new thread begins to execute run()
 7.The different moments each thread terminates

 Note that the new thread's initialization, its execution of run(), and its termination happen simultaneously with the starting thread's execution.
 Also note that after a thread calls start(), subsequent calls to that method before the run() method exits cause start() to throw a java.lang.IllegalThreadStateException object.

 What's in a name?

 During a debugging session, distinguishing one thread from another in a user-friendly fashion proves helpful. To differentiate among threads, Java associates a name with a thread.
 That name defaults to Thread, a hyphen character, and a zero-based integer number. You can accept Java's default thread names or you can choose your own.
 To accommodate custom names, Thread provides constructors that take name arguments and a setName(String name) method. Thread also provides a getName() method that returns the current name.
 NameThread demonstrates how to establish a custom name via the Thread(String name) constructor and retrieve the current name in the run() method by calling getName().

 You can pass an optional name argument to MyThread on the command line. For example, java NameThatThread X establishes X as the thread's name. If you fail to specify a name, you'll see the following output:

 My name is: Thread-1
 If you prefer, you can change the super (name); call in the MyThread (String name) constructor to a call to setName (String name)—as in setName (name);. That latter method call achieves the same objective—establishing the thread's name—as super (name);. I leave that as an exercise for you.

 Naming main
 Java assigns the name main to the thread that runs the main() method, the starting thread. You typically see that name in the Exception in thread "main" message that the JVM's default exception handler prints when the starting thread throws an exception object.

 To sleep or not to sleep

 Later in this column, I will introduce you to animation— repeatedly drawing on one surface images that slightly differ from each other to achieve a movement illusion. To accomplish animation, a thread must pause during its display of two consecutive images. Calling Thread's static sleep(long millis) method forces a thread to pause for millis milliseconds. Another thread could possibly interrupt the sleeping thread. If that happens, the sleeping thread awakes and throws an InterruptedException object from the sleep(long millis) method. As a result, code that calls sleep(long millis) must appear within a try block—or the code's method must include InterruptedException in its throws clause.

 To demonstrate sleep(long millis), I've written a CalcPI1 application. That application starts a new thread that uses a mathematic algorithm to calculate the value of the mathematical constant pi. While the new thread calculates, the starting thread pauses for 10 milliseconds by calling sleep(long millis). After the starting thread awakes, it prints the pi value, which the new thread stores in variable pi. Listing 3 presents CalcPI1's source code:

 If you run this program, you will see output similar (but probably not identical) to the following:

 pi = -0.2146197014017295
 Finished calculating PI

 Why is the output incorrect? After all, pi's value is roughly equivalent to 3.14159. The answer: The starting thread awoke too soon. Just as the new thread was beginning to calculate pi, the starting thread woke up, read pi's current value, and printed that value. We can compensate by increasing the delay from 10 milliseconds to a longer value. That longer value, which (unfortunately) is platform dependent, will give the new thread a chance to complete its calculations before the starting thread awakes. (Later, you will learn about a platform-independent technique that prevents the starting thread from waking until the new thread finishes.)

 Sleeping threads don't lie
 Thread also supplies a sleep(long millis, int nanos) method, which puts the thread to sleep for millis milliseconds and nanos nanoseconds. Because most JVM-based platforms do not support resolutions as small as a nanosecond, JVM thread-handling code rounds the number of nanoseconds to the nearest number of milliseconds. If a platform does not support a resolution as small as a millisecond, JVM thread-handling code rounds the number of milliseconds to the nearest multiple of the smallest resolution that the platform supports.

 Is it dead or alive?

 When a program calls Thread's start() method, a time period (for initialization) passes before a new thread calls run(). After run() returns, a time period passes before the JVM cleans up the thread. The JVM considers the thread to be alive immediately prior to the thread's call to run(), during the thread's execution of run(), and immediately after run() returns. During that interval, Thread's isAlive() method returns a Boolean true value. Otherwise, that method returns false.

 isAlive() proves helpful in situations where a thread needs to wait for another thread to finish its run() method before the first thread can examine the other thread's results. Essentially, the thread that needs to wait enters a while loop. While isAlive() returns true for the other thread, the waiting thread calls sleep(long millis) (or sleep(long millis, int nanos)) to periodically sleep (and avoid wasting many CPU cycles). Once isAlive() returns false, the waiting thread can examine the other thread's results.

 Where would you use such a technique? For starters, how about a modified version of CalcPI1, where the starting thread waits for the new thread to finish before printing pi's value? Listing 4's CalcPI2 source code demonstrates that technique:
 Listing 4. CalcPI2.java

 CalcPI2's starting thread sleeps in 10 millisecond intervals, until mt.isAlive () returns false. When that happens, the starting thread exits from its while loop and prints pi's contents. If you run this program, you will see output similar (but probably not identical) to the following:

 Finished calculating PI
 pi = 3.1415726535897894
 Now doesn't that look more accurate?

 Is it alive?
 A thread could possibly call the isAlive() method on itself. However, that does not make sense because isAlive() will always return true.

 Joining forces

 Because the while loop/isAlive() method/sleep() method technique proves useful, Sun packaged it into a trio of methods: join(), join(long millis), and join(long millis, int nanos). The current thread calls join(), via another thread's thread object reference when it wants to wait for that other thread to terminate. In contrast, the current thread calls join(long millis) or join(long millis, int nanos) when it wants to either wait for that other thread to terminate or wait until a combination of millis millseconds and nanos nanoseconds passes. (As with the sleep() methods, the JVM thread-handling code will round up the argument values of the join(long millis) and join(long millis, int nanos) methods.) Listing 5's CalcPI3 source code demonstrates a call to join():

 Listing 5. CalcPI3.java

 CalcPI3's starting thread waits for the thread that associates with the MyThread object, referenced by mt, to terminate. The starting thread then prints pi's value, which is identical to the value that CalcPI2 outputs.

 Do not attempt to join the current thread to itself because the current thread will wait forever.)

 Census taking

 In some situations, you might want to know which threads are actively running in your program. Thread supplies a pair of methods to help you with that task: activeCount() and enumerate(Thread [] thdarray). But those methods work only in the context of the current thread's thread group. In other words, those methods identify only active threads that belong to the same thread group as the current thread. (I discuss the thread group—an organizational mechanism—concept in a future series article.)

 The static activeCount() method returns a count of the threads actively executing in the current thread's thread group. A program uses this method's integer return value to size an array of Thread references. To retrieve those references, the program must call the static enumerate(Thread [] thdarray) method. That method's integer return value identifies the total number of Thread references that enumerate(Thread []thdarray) stores in the array. To see how these methods work together, check out Listing 6:

 Listing 6. Census.java

When run, this program produces output similar to the following:

Thread[main,5,main]
The output shows that one thread, the starting thread, is running. The leftmost main identifies that thread's name. The 5 indicates that thread's priority, and the rightmost main identifies that thread's thread group. You might be disappointed that you cannot see any system threads, such as the garbage collector thread, in the output. That limitation results from Thread's enumerate(Thread [] thdarray) method, which interrogates only the current thread's thread group for active threads. However, the ThreadGroup class contains multiple enumerate() methods that allow you to capture references to all active threads, regardless of thread group. Later in this series, I will show you how to enumerate all references when I explore ThreadGroup.

activeCount() and NullPointerException
Do not depend on activeCount()'s return value when iterating over an array. If you do, your program runs the risk of throwing NullPointerException objects. Why? Between the calls to activeCount() and enumerate(Thread [] thdarray), one or more threads might possibly terminate. As a result, enumerate(Thread [] thdarray) would copy fewer thread references into its array. Therefore, think of activeCount()'s return value as a maximum value for array-sizing purposes only. Also, think of enumerate(Thread [] thdarray)'s return value as representing the number of active threads at the time of a program's call to that method.

Antibugging

If your program malfunctions, and you suspect that the problem lies with a thread, you can learn details about that thread by calling Thread's dumpStack() and toString() methods. The static dumpStack() method, which provides a wrapper around new Exception ("Stack trace").printStackTrace ();, prints a stack trace for the current thread. toString() returns a String object that describes the thread's name, priority, and thread group according to the following format: Thread[thread-name,priority,thread-group]. (You will learn more about priority later in this series.)

The caste system

Not all threads are created equal. They divide into two categories: user and daemon. A user thread performs important work for the program's user, work that must finish before the application terminates. In contrast, a daemon thread performs housekeeping (such as garbage collection) and other background tasks that probably do not contribute to the application's main work but are necessary for the application to continue its main work. Unlike user threads, daemon threads do not need to finish before the application terminates. When an application's starting thread (which is a user thread) terminates, the JVM checks whether any other user threads are running. If some are, the JVM prevents the application from terminating. Otherwise, the JVM terminates the application regardless of whether daemon threads are running.

When to use currentThread()
In several places, this article refers to the concept of a current thread. If you need access to a Thread object that describes the current thread, call Thread's static currentThread() method. Example: Thread current = Thread.currentThread ();.

When a thread calls a thread object's start() method, the newly started thread is a user thread. That is the default. To establish a thread as a daemon thread, the program must call Thread's setDaemon(boolean isDaemon) method with a Boolean true argument value prior to the call to start(). Later, you can check if a thread is daemon by calling Thread's isDaemon() method. That method returns a Boolean true value if the thread is daemon.

To let you play with user and daemon threads, I wrote UserDaemonThreadDemo:

Listing 7. UserDaemonThreadDemo.java

After compiling the code, run UserDaemonThreadDemo via the Java 2 SDK's java command. If you run the program with no command-line arguments, as in java UserDaemonThreadDemo, for example, new MyThread ().start (); executes. That code fragment starts a user thread that prints Daemon is false prior to entering an infinite loop. (You must press Ctrl-C or an equivalent keystroke combination to terminate that infinite loop.) Because the new thread is a user thread, the application keeps running after the starting thread terminates. However, if you specify at least one command-line argument, as in java UserDaemonThreadDemo x, for example, mt.setDaemon (true); executes, and the new thread will be a daemon. As a result, once the starting thread awakes from its 100-millisecond sleep and terminates, the new daemon thread will also terminate.

A setDaemon() exception
Note that the setDaemon(boolean isDaemon) method throws an IllegalThreadStateException object if a call is made to that method after the thread starts execution.

Runnables

After studying the previous section's examples, you might think that introducing multithreading into a class always requires you to extend Thread and have your subclass override Thread's run() method. That is not always an option, however. Java's enforcement of implementation inheritance prohibits a class from extending two or more superclasses. As a result, if a class extends a non-Thread class, that class cannot also extend Thread. Given that restriction, how is it possible to introduce multithreading into a class that already extends some other class? Fortunately, Java's designers realized that situations would arise where subclassing Thread wouldn't be possible. That realization led to the java.lang.Runnable interface and Thread constructors with Runnable parameters, such as Thread(Runnable target).

The Runnable interface declares a single method signature: void run();. That signature is identical to Thread's run() method signature and serves as a thread's entry of execution. Because Runnable is an interface, any class can implement that interface by attaching an implements clause to the class header and by providing an appropriate run() method. At execution time, program code can create an object, or runnable, from that class and pass the runnable's reference to an appropriate Thread constructor. The constructor stores that reference within the Thread object and ensures that a new thread calls the runnable's run() method after a call to the Thread object's start() method, which Listing 8 demonstrates:

Listing 8. RunnableDemo.java


RunnableDemo describes an applet for repeatedly outputting asterisk-based rectangle outlines on the standard output. To accomplish this task, Runnable must extend the java.applet.Applet class (java.applet identifies the package in which Applet is located -- I discuss packages in a future article) and implement the Runnable interface.

An applet provides a public void start() method, which is called (typically by a Web browser) when an applet is to start running, and provides a public void stop() method, which is called when an applet is to stop running.

The start() method is the perfect place to create and start a thread, and RunnableDemo accomplishes this task by executing t = new Thread (this); t.start ();. I pass this to Thread's constructor because the applet is a runnable due to RunnableDemo implementing Runnable.

The stop() method is the perfect place to stop a thread, by assigning null to the Thread variable. I cannot use Thread's public void stop() method for this task because this method has been deprecated -- it's unsafe to use.

The run() method contains an infinite loop that runs for as long as Thread.currentThread() returns the same Thread reference as located in Thread variable t. The reference in this variable is nullified when the applet's stop() method is called.

Because RunnableDemo's new output would prove too lengthy to include with this article, I suggest you compile and run that program yourself.

You will need to use the appletviewer tool and an HTML file to run the applet. Listing 9 presents a suitable HTML file -- the width and height are set to 0 because no graphical output is generated.

Listing 9. RunnableDemo.html

<applet code="RunnableDemo" width="0" height="0"></applet>

Specify appletviewer RunnableDemo.html to run this applet.

Thread vs Runnable?
When you face a situation where a class can either extend Thread or implement Runnable, which approach do you choose? If the class already extends another class, you must implement Runnable. However, if that class extends no other class, think about the class name. That name will suggest that the class's objects are either active or passive. For example, the name Ticker suggests that its objects are active—they tick. Thus, the Ticker class would extend Thread, and Ticker objects would be specialized Thread objects.

Review

Users expect programs to achieve strong performance. One way to accomplish that task is to use threads. A thread is an independent path of execution through program code. Threads benefit GUI-based programs because they allow those programs to remain responsive to users while performing other tasks. In addition, threaded programs typically finish faster than their nonthreaded counterparts. This is especially true of threads running on a multiprocessor machine, where each thread has its own processor. The Thread and Thread subclass objects describe threads and associate with those entities. For those classes that cannot extend Thread, you must create a runnable to take advantage of multithreading.

Next month, I continue this series by showing you how to synchronize access to shared data.

