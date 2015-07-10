package com.acme.lang.thread;

public class PrintThread extends Thread {

    public PrintThread() {
        // The compiler creates the byte code equivalent of super ();
    }

    public PrintThread(String name) {
        super(name); // Pass name to Thread superclass
    }

    public void run() {
        System.out.println("My name is: " + getName());
    }
}
