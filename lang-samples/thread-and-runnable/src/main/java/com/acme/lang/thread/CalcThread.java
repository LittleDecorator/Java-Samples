package com.acme.lang.thread;

public class CalcThread extends Thread
{
    boolean negative = true;
    public double pi; // Initializes to 0.0, by default
    public void run ()
    {
        for (int i = 3; i < 100000; i += 2)
        {
            if (negative)
                pi -= (1.0 / i);
            else
                pi += (1.0 / i);
            negative = !negative;
        }
        pi += 1.0;
        pi *= 4.0;
        System.out.println ("Finished calculating PI");
    }
}
