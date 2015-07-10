package com.acme.lang.sync;

import com.acme.lang.sync.model.FinTrans;
import com.acme.lang.sync.thread.UpTransThread;

public class UpSyncSample {

    public static void main (String [] args)
    {
        FinTrans ft = new FinTrans();
        UpTransThread tt1 = new UpTransThread (ft, "Deposit Thread");
        UpTransThread tt2 = new UpTransThread(ft, "Withdrawal Thread");
        tt1.start ();
        tt2.start ();
    }

}
