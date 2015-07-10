package com.acme.lang.thread;

public class DaemonThread extends Thread {
    public void run (){
        System.out.println ("Daemon is " + isDaemon ());
        while (true);
    }
}
