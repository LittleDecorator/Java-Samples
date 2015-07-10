package com.acme.lang.sync;

import com.acme.lang.sync.model.FinTrans;
import com.acme.lang.sync.thread.DeadTransThread;

public class DeadlockSample {

    public static void main (String [] args)
    {
        FinTrans ft = new FinTrans();
        DeadTransThread tt1 = new DeadTransThread(ft, "Deposit Thread");
        DeadTransThread tt2 = new DeadTransThread (ft, "Withdrawal Thread");
        tt1.start ();
        tt2.start ();
    }

}
