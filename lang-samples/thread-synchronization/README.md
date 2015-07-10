Multithreaded programs often function erratically or produce erroneous values due to the lack of thread synchronization. Synchronization is the act of serializing (or ordering one at a time) thread access to those code sequences that let multiple threads manipulate class and instance field variables, and other shared resources. I call those code sequences critical code sections.. This month's column is all about using synchronization to serialize thread access to critical code sections in your programs.

I begin with an example that illustrates why some multithreaded programs must use synchronization. I next explore Java's synchronization mechanism in terms of monitors and locks, and the synchronized keyword. Because incorrectly using the synchronization mechanism negates its benefits, I conclude by investigating two problems that result from such misuse.

Tip: Unlike class and instance field variables, threads cannot share local variables and parameters. The reason: Local variables and parameters allocate on a thread's method-call stack. As a result, each thread receives its own copy of those variables. In contrast, threads can share class fields and instance fields because those variables do not allocate on a thread's method-call stack. Instead, they allocate in shared heap memory—as part of classes (class fields) or objects (instance fields).

The need for synchronization

Why do we need synchronization? For an answer, consider this example: You write a Java program that uses a pair of threads to simulate withdrawal/deposit of financial transactions. In that program, one thread performs deposits while the other performs withdrawals. Each thread manipulates a pair of shared variables, class and instance field variables, that identifies the financial transaction's name and amount. For a correct financial transaction, each thread must finish assigning values to the name and amount variables (and print those values, to simulate saving the transaction) before the other thread starts assigning values to name and amount (and also printing those values). After some work, you end up with source code that resembles Listing 1:

Listing 1. NeedForSynchronizationDemo.java

NeedForSynchronizationDemo's source code has two critical code sections: one accessible to the deposit thread, and the other accessible to the withdrawal thread. Within the deposit thread's critical code section, that thread assigns the Deposit String object's reference to shared variable transName and assigns 2000.0 to shared variable amount. Similarly, within the withdrawal thread's critical code section, that thread assigns the Withdrawal String object's reference to transName and assigns 250.0 to amount. Following each thread's assignments, those variables' contents print. When you run NeedForSynchronizationDemo, you might expect output similar to a list of interspersed Withdrawal 250.0 and Deposit 2000.0 lines. Instead, you receive output resembling the following:

Withdrawal 250.0
Withdrawal 2000.0
Deposit 2000.0
Deposit 2000.0
Deposit 250.0
The program definitely has a problem. The withdrawal thread should not be simulating $2000 withdrawals, and the deposit thread should not be simulating $250 deposits. Each thread produces inconsistent output. What causes those inconsistencies? Consider the following:

On a single-processor machine, threads share the processor. As a result, one thread can only execute for a certain time period. At that time, the JVM/operating system pauses that thread's execution and allows another thread to execute—a manifestation of thread scheduling, a topic I discuss in Part 3. On a multiprocessor machine, depending on the number of threads and processors, each thread can have its own processor.
On a single-processor machine, a thread's execution period might not last long enough for that thread to finish executing its critical code section before another thread begins executing its own critical code section. On a multiprocessor machine, threads can simultaneously execute code in their critical code sections. However, they might enter their critical code sections at different times.
On either single-processor or multiprocessor machines, the following scenario can occur: Thread A assigns a value to shared variable X in its critical code section and decides to perform an input/output operation that requires 100 milliseconds. Thread B then enters its critical code section, assigns a different value to X, performs a 50-millisecond input/output operation, and assigns values to shared variables Y and Z. Thread A's input/output operation completes, and that thread assigns its own values to Y and Z. Because X contains a B-assigned value, whereas Y and Z contain A-assigned values, an inconsistency results.

How does an inconsistency arise in NeedForSynchronizationDemo? Suppose the deposit thread executes ft.transName = "Deposit"; and then calls Thread.sleep(). At that point, the deposit thread surrenders control of the processor for the time period it must sleep, and the withdrawal thread executes. Assume the deposit thread sleeps for 500 milliseconds (a randomly selected value, thanks to Math.random(), from the inclusive range 0 through 999 milliseconds; I explore Math and its random() method in a future article). During the deposit thread's sleep time, the withdrawal thread executes ft.transName = "Withdrawal";, sleeps for 50 milliseconds (the withdrawal thread's randomly selected sleep value), awakes, executes ft.amount = 250.0;, and executes System.out.println (ft.transName + " " + ft.amount);—all before the deposit thread awakes. As a result, the withdrawal thread prints Withdrawal 250.0, which is correct. When the deposit thread awakes, it executes ft.amount = 2000.0;, followed by System.out.println (ft.transName + " " + ft.amount);. This time, Withdrawal 2000.0 prints, which is not correct. Although the deposit thread previously assigned the "Deposit"'s reference to transName, that reference subsequently disappeared when the withdrawal thread assigned the "Withdrawal"'s reference to that shared variable. When the deposit thread awoke, it failed to restore the correct reference to transName, but continued its execution by assigning 2000.0 to amount. Although neither variable has an invalid value, the combined values of both variables represent an inconsistency. In this case, their values represent an attempt to withdraw ,000.

Long ago, computer scientists invented a term to describe the combined behaviors of multiple threads that lead to inconsistencies. That term is race condition—the act of each thread racing to complete its critical code section before some other thread enters that same critical code section. As NeedForSynchronizationDemo demonstrates, threads' execution orders are unpredictable. There is no guarantee that a thread can complete its critical code section before some other thread enters that section. Hence, we have a race condition, which causes inconsistencies. To prevent race conditions, each thread must complete its critical code section before another thread enters either the same critical code section or another related critical code section that manipulates the same shared variables or resources. With no means of serializing access—that is, allowing access to only one thread at a time —to a critical code section, you can't prevent race conditions or inconsistencies. Fortunately, Java provides a way to serialize thread access: through its synchronization mechanism.

Note: Of Java's types, only long integer and double-precision floating-point variables are prone to inconsistencies. Why? A 32-bit JVM typically accesses a 64-bit long integer variable or a 64-bit double-precision floating-point variable in two adjacent 32-bit steps. One thread might complete the first step and then wait while another thread executes both steps. Then, the first thread might awake and complete the second step, producing a variable with a value different from either the first or second thread's value. As a result, if at least one thread can modify either a long integer variable or a double-precision floating-point variable, all threads that read and/or modify that variable must use synchronization to serialize access to the variable.

Java's synchronization mechanism

Java provides a synchronization mechanism for preventing more than one thread from executing code in one or more critical code sections at any point in time. That mechanism bases itself on the concepts of monitors and locks. Think of a monitor as a protective wrapper around a critical code section and a lock as a software entity that a monitor uses to prevent multiple threads from entering the monitor. The idea is this: When a thread wishes to enter a monitor-guarded critical code section, that thread must acquire the lock associated with an object that associates with the monitor. (Each object has its own lock.) If some other thread holds that lock, the JVM forces the requesting thread to wait in a waiting area associated with the monitor/lock. When the thread in the monitor releases the lock, the JVM removes the waiting thread from the monitor's waiting area and allows that thread to acquire the lock and proceed to the monitor's critical code section.

To work with monitors/locks, the JVM provides the monitorenter and monitorexit instructions. Fortunately, you do not need to work at such a low level. Instead, you can use Java's synchronized keyword in the context of the synchronized statement and synchronized methods.

The synchronized statement

Some critical code sections occupy small portions of their enclosing methods. To guard multiple thread access to such critical code sections, you use the synchronized statement. That statement has the following syntax:

'synchronized' '(' objectidentifier ')'
'{'
   // Critical code section
'}'
The synchronized statement begins with keyword synchronized and continues with an objectidentifier, which appears between a pair of round brackets. The objectidentifier references an object whose lock associates with the monitor that the synchronized statement represents. Finally, the Java statements' critical code section appears between a pair of brace characters. How do you interpret the synchronized statement? Consider the following code fragment:

synchronized ("sync object")
{
   // Access shared variables and other shared resources
}

From a source code perspective, a thread attempts to enter the critical code section that the synchronized statement guards. Internally, the JVM checks if some other thread holds the lock associated with the "sync object" object. (Yes, "sync object" is an object. You will understand why in a future article.) If no other thread holds the lock, the JVM gives the lock to the requesting thread and allows that thread to enter the critical code section between the brace characters. However, if some other thread holds the lock, the JVM forces the requesting thread to wait in a private waiting area until the thread currently within the critical code section finishes executing the final statement and transitions past the final brace character.

You can use the synchronized statement to eliminate NeedForSynchronizationDemo's race condition. To see how, examine Listing 2:

Listing 2. SynchronizationDemo1.java

Look carefully at SynchronizationDemo1; the run() method contains two critical code sections sandwiched between synchronized (ft) { and }. Each of the deposit and withdrawal threads must acquire the lock that associates with the FinTrans object that ft references before either thread can enter its critical code section. If, for example, the deposit thread is in its critical code section and the withdrawal thread wants to enter its own critical code section, the withdrawal thread attempts to acquire the lock. Because the deposit thread holds that lock while it executes within its critical code section, the JVM forces the withdrawal thread to wait until the deposit thread executes that critical code section and releases the lock. (When execution leaves the critical code section, the lock releases automatically.)

Tip: When you need to determine if a thread holds a given object's associated lock, call Thread's static boolean holdsLock(Object o) method. That method returns a Boolean true value if the thread calling that method holds the lock associated with the object that o references; otherwise, false returns. For example, if you were to place System.out.println (Thread.holdsLock (ft)); at the end of SynchronizationDemo1's main() method, holdsLock() would return false. False would return because the main thread executing the main() method does not use the synchronization mechanism to acquire any lock. However, if you were to place System.out.println (Thread.holdsLock (ft)); in either of run()'s synchronized (ft) statements, holdsLock() would return true because either the deposit thread or the withdrawal thread had to acquire the lock associated with the FinTrans object that ft references before that thread could enter its critical code section.

Synchronized methods

You can employ synchronized statements throughout your program's source code. However, you might run into situations where excessive use of such statements leads to inefficient code. For example, suppose your program contains a method with two successive synchronized statements that each attempt to acquire the same common object's associated lock. Because acquiring and releasing the object's lock eats up time, repeated calls (in a loop) to that method can degrade the program's performance. Each time a call is made to that method, it must acquire and release two locks. The greater the number of lock acquisitions and releases, the more time the program spends acquiring and releasing the locks. To get around that problem, you might consider using a synchronized method.

A synchronized method is either an instance or class method whose header includes the synchronized keyword. For example: synchronized void print (String s). When you synchronize an entire instance method, a thread must acquire the lock associated with the object on which the method call occurs. For example, given an ft.update("Deposit", 2000.0); instance method call, and assuming that update() is synchronized, a thread must acquire the lock associated with the object that ft references. To see a synchronized method version of the SynchronizationDemo1 source code, check out Listing 3:

Listing 3. SynchronizationDemo2.java

Though slightly more compact than Listing 2, Listing 3 accomplishes the same purpose. If the deposit thread calls the update() method, the JVM checks to see if the withdrawal thread has acquired the lock associated with the object that ft references. If so, the deposit thread waits. Otherwise, that thread enters the critical code section.

SynchronizationDemo2 demonstrates a synchronized instance method. However, you can also synchronize class methods. For example, the java.util.Calendar class declares a public static synchronized Locale [] getAvailableLocales() method. Because class methods have no concept of a this reference, from where does the class method acquire its lock? Class methods acquire their locks from class objects—each loaded class associates with a Class object, from which the loaded class's class methods obtain their locks. I refer to such locks as class locks.

Caution: Don't synchronize a thread object's run() method because situations arise where multiple threads need to execute run(). Because those threads attempt to synchronize on the same object, only one thread at a time can execute run(). As a result, each thread must wait for the previous thread to terminate before it can access run().

Some programs intermix synchronized instance methods and synchronized class methods. To help you understand what happens in programs where synchronized class methods call synchronized instance methods and vice-versa (via object references), keep the following two points in mind:

Object locks and class locks do not relate to each other. They are different entities. You acquire and release each lock independently. A synchronized instance method calling a synchronized class method acquires both locks. First, the synchronized instance method acquires its object's object lock. Second, that method acquires the synchronized class method's class lock.
Synchronized class methods can call an object's synchronized methods or use the object to lock a synchronized block. In that scenario, a thread initially acquires the synchronized class method's class lock and subsequently acquires the object's object lock. Hence, a synchronized class method calling a synchronized instance method also acquires two locks.
The following code fragment illustrates the second point:

class LockTypes
{
   // Object lock acquired just before execution passes into instanceMethod()
   synchronized void instanceMethod ()
   {
      // Object lock released as thread exits instanceMethod()
   }
   // Class lock acquired just before execution passes into classMethod()
   synchronized static void classMethod (LockTypes lt)
   {
      lt.instanceMethod ();
      // Object lock acquired just before critical code section executes

      synchronized (lt)
      {
         // Critical code section
         // Object lock released as thread exits critical code section
      }
      // Class lock released as thread exits classMethod()
   }
}

The code fragment demonstrates synchronized class method classMethod() calling synchronized instance method instanceMethod(). By reading the comments, you see that classMethod() first acquires its class lock and then acquires the object lock associated with the LockTypes object that lt references.

Two problems with the synchronization mechanism

Despite its simplicity, developers often misuse Java's synchronization mechanism, which causes problems ranging from no synchronization to deadlock. This section examines these problems and provides a pair of recommendations for avoiding them.

Note: A third problem related to the synchronization mechanism is the time cost associated with lock acquisition and release. In other words, it takes time for a thread to acquire or release a lock. When acquiring/releasing a lock in a loop, individual time costs add up, which can degrade performance. For older JVMs, the lock-acquisition time cost often results in significant performance penalties. Fortunately, Sun Microsystems' HotSpot JVM (which ships with Sun's Java 2 Platform, Standard Edition (J2SE) SDK) offers fast lock acquisition and release, greatly reducing this problem's impact.

No synchronization

After a thread voluntarily or involuntarily (through an exception) exits a critical code section, it releases a lock so another thread can gain entry. Suppose two threads want to enter the same critical code section. To prevent both threads from entering that critical code section simultaneously, each thread must attempt to acquire the same lock. If each thread attempts to acquire a different lock and succeeds, both threads enter the critical code section; neither thread has to wait for the other thread to release its lock because the other thread acquires a different lock. The end result: no synchronization, as demonstrated in Listing 4:

Listing 4. NoSynchronizationDemo.java

When you run NoSynchronizationDemo, you will see output resembling the following excerpt:

Withdrawal 250.0
Withdrawal 2000.0
Deposit 250.0
Withdrawal 2000.0
Deposit 2000.0


Despite the use of synchronized statements, no synchronization takes place. Why? Examine synchronized (this). Because keyword this refers to the current object, the deposit thread attempts to acquire the lock associated with the TransThread object whose reference initially assigns to tt1 (in the main() method). Similarly, the withdrawal thread attempts to acquire the lock associated with the TransThread object whose reference initially assigns to tt2. We have two different TransThread objects, and each thread attempts to acquire the lock associated with its respective TransThread object before entering its own critical code section. Because the threads acquire different locks, both threads can be in their own critical code sections at the same time. The result is no synchronization.

Tip: To avoid a no-synchronization scenario, choose an object common to all relevant threads. That way, those threads compete to acquire the same object's lock, and only one thread at a time can enter the associated critical code section.

Deadlock

In some programs, the following scenario might occur: Thread A acquires a lock that thread B needs before thread B can enter B's critical code section. Similarly, thread B acquires a lock that thread A needs before thread A can enter A's critical code section. Because neither thread has the lock it needs, each thread must wait to acquire its lock. Furthermore, because neither thread can proceed, neither thread can release the other thread's lock, and program execution freezes. This behavior is known as deadlock, which Listing 5 demonstrates:

Listing 5. DeadlockDemo.java

If you run DeadlockDemo, you will probably see only a single line of output before the application freezes. To unfreeze DeadlockDemo, press Ctrl-C (assuming you are using Sun's SDK 1.4 toolkit at a Windows command prompt).

What causes the deadlock? Look carefully at the source code; the deposit thread must acquire two locks before it can enter its innermost critical code section. The outer lock associates with the FinTrans object that ft references, and the inner lock associates with the String object that anotherSharedLock references. Similarly, the withdrawal thread must acquire two locks before it can enter its own innermost critical code section. The outer lock associates with the String object that anotherSharedLock references, and the inner lock associates with the FinTrans object that ft references. Suppose both threads' execution orders are such that each thread acquires its outer lock. Thus, the deposit thread acquires its FinTrans lock, and the withdrawal thread acquires its String lock. Now that both threads possess their outer locks, they are in their appropriate outer critical code section. Both threads then attempt to acquire the inner locks, so they can enter the appropriate inner critical code sections.

The deposit thread attempts to acquire the lock associated with the anotherSharedLock-referenced object. However, the deposit thread must wait because the withdrawal thread holds that lock. Similarly, the withdrawal thread attempts to acquire the lock associated with the ft-referenced object. But the withdraw thread cannot acquire that lock because the deposit thread (which is waiting) holds it. Therefore, the withdrawal thread must also wait. Neither thread can proceed because neither thread releases the lock it holds. And neither thread can release the lock it holds because each thread is waiting. Each thread deadlocks, and the program freezes.

Tip: To avoid deadlock, carefully analyze your source code for situations where threads might attempt to acquire each others' locks, such as when a synchronized method calls another synchronized method. You must do that because a JVM cannot detect or prevent deadlock.

Review

To achieve strong performance with threads, you will encounter situations where your multithreaded programs need to serialize access to critical code sections. Known as synchronization, that activity prevents inconsistencies resulting in strange program behavior. You can use either synchronized statements to guard portions of a method, or synchronize the entire method. But comb your code carefully for glitches that can result in failed synchronization or deadlocks.