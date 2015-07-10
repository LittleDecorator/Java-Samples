package com.acme.lang;

import com.acme.lang.thread.CalcThread;

public class WrongCalcPi {

    public static void main (String [] args){
        CalcThread mt = new CalcThread();
        mt.start ();
        try
        {
            Thread.sleep (10); // Sleep for 10 milliseconds
        }
        catch (InterruptedException e)
        {
        }
        System.out.println ("pi = " + mt.pi);
    }
}


