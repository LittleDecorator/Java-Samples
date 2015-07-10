package com.acme.lang;

import com.acme.lang.thread.CycleThread;

public class SimpleThreadSample {

    public static void main (String [] args) {
        CycleThread mt = new CycleThread();
        mt.start ();
        for (int i = 0; i < 50; i++){
            System.out.println ("i = " + i + ", i * i = " + i * i);
        }
    }

}


