package com.acme.lang;

public class CorrectCalcPi {

    public static void main (String [] args)
    {
        CalcThread mt = new CalcThread();
        mt.start ();
        while (mt.isAlive ())
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
