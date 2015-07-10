package com.acme.lang.sync.model;

public class FinTrans {
    public static String transName;
    public static double amount;

    public synchronized void update (String transName, double amount)
    {
        this.transName = transName;
        this.amount = amount;
        System.out.println (this.transName + " " + this.amount);
    }
}
