package com.acme.lang.sync;

import com.acme.lang.sync.model.FinTrans;
import com.acme.lang.sync.thread.WrongTransThread;

public class WrongSyncSample {

    public static void main (String [] args)
    {
        FinTrans ft = new FinTrans ();
        WrongTransThread tt1 = new WrongTransThread(ft, "Deposit Thread");
        WrongTransThread tt2 = new WrongTransThread (ft, "Withdrawal Thread");
        tt1.start ();
        tt2.start ();
    }

}
