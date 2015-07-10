package com.acme.lang;

import com.acme.lang.thread.CalculationThread;

public class ScheduleSample {

    public static void main (String [] args)
    {
        new CalculationThread ("CalcThread A").start ();
        new CalculationThread("CalcThread B").start ();
    }

}
