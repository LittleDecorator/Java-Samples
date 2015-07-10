package com.acme.lang;

import com.acme.lang.thread.SimpleThread;

public class InterruptThreadSample {

    public static void main (String [] args){
        SimpleThread mt = new SimpleThread ();
        mt.setName ("A");
        mt.start ();
        mt = new SimpleThread ();
        mt.setName ("B");
        mt.start ();
        try{
            Thread.sleep (2000); // Wait 2 seconds
        } catch (InterruptedException e){}
        // Interrupt all methods in the same thread group as the main
        // thread
        Thread.currentThread ().getThreadGroup ().interrupt ();
    }

}
