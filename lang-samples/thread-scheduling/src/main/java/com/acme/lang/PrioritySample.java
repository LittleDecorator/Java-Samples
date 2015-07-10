package com.acme.lang;

import com.acme.lang.thread.BlockingThread;
import com.acme.lang.thread.SummingThread;

public class PrioritySample {

    public static void main (String [] args)
    {
        BlockingThread bt = new BlockingThread();
        bt.setPriority (Thread.NORM_PRIORITY + 1);
        SummingThread ct = new SummingThread();
        bt.start ();
        ct.start ();
        try
        {
            Thread.sleep (10000);
        }
        catch (InterruptedException e)
        {
        }
        bt.setFinished (true);
        ct.setFinished (true);
    }

}
