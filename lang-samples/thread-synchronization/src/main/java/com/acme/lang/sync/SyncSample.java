package com.acme.lang.sync;

import com.acme.lang.sync.model.FinTrans;
import com.acme.lang.sync.thread.SyncTransThread;

/**
 * Created by nikolay on 10.07.15.
 */
public class SyncSample {
    public static void main (String [] args)
    {
        FinTrans ft = new FinTrans();
        SyncTransThread tt1 = new SyncTransThread(ft, "Deposit Thread");
        SyncTransThread tt2 = new SyncTransThread (ft, "Withdrawal Thread");
        tt1.start ();
        tt2.start ();
    }
}
