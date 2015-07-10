package com.acme.lang;

import com.acme.lang.thread.DaemonThread;

public class UserDaemonThreadSample {

    public static void main (String [] args){
        if (args.length == 0)
            new DaemonThread().start ();
        else
        {
            DaemonThread mt = new DaemonThread();
            mt.setDaemon (true);
            mt.start ();
        }
        try
        {
            Thread.sleep (100);
        }
        catch (InterruptedException e)
        {
        }
    }

}
