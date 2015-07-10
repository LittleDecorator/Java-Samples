package com.acme.lang.sync;

import com.acme.lang.sync.model.FinTrans;
import com.acme.lang.sync.thread.TransThread;

public class NoSyncSample {
    public static void main (String [] args)
    {
        FinTrans ft = new FinTrans ();
        TransThread tt1 = new TransThread (ft, "Deposit Thread");
        TransThread tt2 = new TransThread (ft, "Withdrawal Thread");
        tt1.start ();
        tt2.start ();
    }
}
