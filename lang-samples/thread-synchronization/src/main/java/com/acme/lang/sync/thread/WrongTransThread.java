package com.acme.lang.sync.thread;

import com.acme.lang.sync.model.FinTrans;

public class WrongTransThread extends Thread
{
    private FinTrans ft;
    public WrongTransThread (FinTrans ft, String name){
        super (name); // Save thread's name
        this.ft = ft; // Save reference to financial transaction object
    }

    @Override
    public void run (){
        for (int i = 0; i < 100; i++){
            if (getName ().equals ("Deposit Thread")){
                synchronized (this){
                    ft.transName = "Deposit";
                    try{
                        Thread.sleep ((int) (Math.random () * 1000));
                    } catch (InterruptedException e){}
                    ft.amount = 2000.0;
                    System.out.println (ft.transName + " " + ft.amount);
                }
            } else {
                synchronized (this) {
                    ft.transName = "Withdrawal";
                    try{
                        Thread.sleep ((int) (Math.random () * 1000));
                    } catch (InterruptedException e) { }
                    ft.amount = 250.0;
                    System.out.println (ft.transName + " " + ft.amount);
                }
            }
        }
    }
}
