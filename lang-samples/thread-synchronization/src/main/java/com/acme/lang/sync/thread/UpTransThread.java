package com.acme.lang.sync.thread;

import com.acme.lang.sync.model.FinTrans;

public class UpTransThread extends Thread {
    private FinTrans ft;
    public UpTransThread (FinTrans ft, String name)
    {
        super (name); // Save thread's name
        this.ft = ft; // Save reference to financial transaction object
    }
    public void run ()
    {
        for (int i = 0; i < 100; i++)
            if (getName ().equals ("Deposit Thread"))
                ft.update ("Deposit", 2000.0);
            else
                ft.update ("Withdrawal", 250.0);
    }
}
